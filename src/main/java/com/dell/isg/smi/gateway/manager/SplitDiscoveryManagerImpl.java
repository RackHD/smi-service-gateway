package com.dell.isg.smi.gateway.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceTypes;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceGroupEnum;
import com.dell.isg.smi.gateway.client.DeviceDiscoveryClient;

@Component
public class SplitDiscoveryManagerImpl implements ISplitDiscoveryManager {

	@Autowired
	private DeviceDiscoveryClient discoveryClient;

	private static final Logger logger = LoggerFactory.getLogger(SplitDiscoveryManagerImpl.class.getName());

	@Override
	public List<DiscoverdDeviceResponse> process(DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests)
			throws Exception {
		Set<DiscoverDeviceRequest> ranges = discoverIPRangeDeviceRequests.getDiscoverIpRangeDeviceRequests();
		List<DiscoverdDeviceResponse> responseList = initializeResponseList();
		if (ranges.size() == 1) {
			try {
				discoverIPRangeDeviceRequests = splitIpv4ranges(discoverIPRangeDeviceRequests);
				ranges = discoverIPRangeDeviceRequests.getDiscoverIpRangeDeviceRequests();
			} catch (Exception e) {
				logger.error("Error while splitting the ranges hence forwarding the request to service to handle the error.");
			}
			//logger.trace("Discovery Request : {} "+ ReflectionToStringBuilder.toString(discoverIPRangeDeviceRequests, new CustomRecursiveToStringStyle(99)));
			//System.out.println("Discovery Request : {} "+ ReflectionToStringBuilder.toString(discoverIPRangeDeviceRequests, new CustomRecursiveToStringStyle(99)));
		}
		ExecutorService executorService = Executors.newFixedThreadPool(ranges.size());
		Set<Callable<List<DiscoverdDeviceResponse>>> callables = new HashSet<Callable<List<DiscoverdDeviceResponse>>>();
		for (DiscoverDeviceRequest discoverDeviceRequest : ranges) {
			DiscoverIPRangeDeviceRequests newDiscoverIPRangeDeviceRequests = new DiscoverIPRangeDeviceRequests();
			newDiscoverIPRangeDeviceRequests.setCredential(discoverIPRangeDeviceRequests.getCredential());
			Set<DiscoverDeviceRequest> newRanges = new HashSet<DiscoverDeviceRequest>();
			newRanges.add(discoverDeviceRequest);
			newDiscoverIPRangeDeviceRequests.setDiscoverIpRangeDeviceRequests(newRanges);
			callables.add(new Callable<List<DiscoverdDeviceResponse>>() {
				public List<DiscoverdDeviceResponse> call() throws Exception {
					return discoveryClient.discover(newDiscoverIPRangeDeviceRequests);
				}
			});
		}
		List<Future<List<DiscoverdDeviceResponse>>> futures = executorService.invokeAll(callables);
		for (Future<List<DiscoverdDeviceResponse>> future : futures) {
			aggregateResponses(responseList, future.get());
		}
		executorService.shutdown();
		return responseList;
	}

	private List<DiscoverdDeviceResponse> initializeResponseList() {
		List<DiscoverdDeviceResponse> responseList = new ArrayList<DiscoverdDeviceResponse>();
		for (DiscoveryDeviceGroupEnum enumGroupName : DiscoveryDeviceGroupEnum.values()) {
			DiscoverdDeviceResponse discoverdDeviceResponse = new DiscoverdDeviceResponse();
			discoverdDeviceResponse.setDeviceGroup(enumGroupName.value());
			discoverdDeviceResponse.setDiscoveredDeviceTypesList(new ArrayList<DiscoveredDeviceTypes>());
			responseList.add(discoverdDeviceResponse);
		}
		return responseList;
	}

	private void aggregateResponses(List<DiscoverdDeviceResponse> gatewayResponse,
			List<DiscoverdDeviceResponse> serviceResponse) {
		for (DiscoverdDeviceResponse discoverdDevice : serviceResponse) {
			DiscoverdDeviceResponse aggregateDiscoverdDevice = gatewayResponse.stream()
					.filter(s -> s.getDeviceGroup().equals(discoverdDevice.getDeviceGroup())).findFirst().get();
			int indexGroup = gatewayResponse.indexOf(aggregateDiscoverdDevice);
			List<DiscoveredDeviceTypes> discoveredDeviceTypes = discoverdDevice.getDiscoveredDeviceList();
			for (DiscoveredDeviceTypes deviceType : discoveredDeviceTypes) {
				DiscoveredDeviceTypes aggregateDeviceTypes = null;
				try {
					aggregateDeviceTypes = aggregateDiscoverdDevice.getDiscoveredDeviceList().stream()
							.filter(s -> s.getDeviceName().equals(deviceType.getDeviceName())).findFirst().get();
				} catch (Exception e) {
					logger.debug("No device type list in aggregation response. hence constructing new one.");
				}
				if (aggregateDeviceTypes == null) {
					aggregateDiscoverdDevice.getDiscoveredDeviceList().add(deviceType);
				} else {
					int indexType = aggregateDiscoverdDevice.getDiscoveredDeviceList().indexOf(aggregateDeviceTypes);
					aggregateDeviceTypes.getDiscoveredDeviceInfoList().addAll(deviceType.getDiscoveredDeviceInfoList());
					aggregateDeviceTypes.setDiscovered(aggregateDeviceTypes.getDiscoveredDeviceInfoList().size());
					aggregateDiscoverdDevice.getDiscoveredDeviceList().set(indexType, aggregateDeviceTypes);
				}
				gatewayResponse.set(indexGroup, aggregateDiscoverdDevice);
			}

		}

	}

	private DiscoverIPRangeDeviceRequests splitIpv4ranges(DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests) {
		DiscoverDeviceRequest discoverDeviceRequest = discoverIPRangeDeviceRequests.getDiscoverIpRangeDeviceRequests()
				.iterator().next();
		Set<DiscoverDeviceRequest> newRanges = new HashSet<DiscoverDeviceRequest>();
		String[] lastIp = StringUtils.split(discoverDeviceRequest.getDeviceEndIp(), ".");
		String[] firstIp = StringUtils.split(discoverDeviceRequest.getDeviceStartIp(),".");
		int current = 0;
		int last = 0;
		for (int i = 0; i <= 3; i++) {
			current |= (Integer.parseInt(firstIp[i])) << ((3 - i) * 8);
			last |= (Integer.parseInt(lastIp[i])) << ((3 - i) * 8);
		}
		if ((current & 0xffffff00) == (last & 0xffffff00)) { // this is a valid
																// range
			return discoverIPRangeDeviceRequests;
		}
		String[] start = new String[4];
		String[] end = new String[4];
		DiscoverDeviceRequest firstRange = new DiscoverDeviceRequest(discoverDeviceRequest);
		current |= 0xFF;
		for (int i = 0; i <= 3; i++) {
			end[i] = String.valueOf((current >> ((3 - i) * 8)) & 0xff);
		}
		firstRange.setDeviceEndIp(StringUtils.join(end,"."));
		newRanges.add(firstRange);
		current += 1; // increment from x.x.x.255 to x.x.x.1
		while ((current & 0xffffff00) != (last & 0xffffff00)) {
			for (int i = 0; i <= 3; i++) {
				start[i] = String.valueOf((current >> ((3 - i) * 8)) & 0xff);
				if (i == 3) {
					end[i] = String.valueOf(0xFF);
				} else {
					end[i] = String.valueOf((current >> ((3 - i) * 8)) & 0xff);
				}
			}
			DiscoverDeviceRequest loopRange = new DiscoverDeviceRequest(discoverDeviceRequest);
			loopRange.setDeviceStartIp(StringUtils.join(start,"."));
			loopRange.setDeviceEndIp(StringUtils.join(end,"."));
			newRanges.add(loopRange);
			current += 256; // increment from x.x.x.255 to x.x.x.1
		}

		for (int i = 0; i <= 3; i++) {
			start[i] = String.valueOf((current >> ((3 - i) * 8)) & 0xff);
			end[i] = String.valueOf((last >> ((3 - i) * 8)) & 0xff);
		}
		DiscoverDeviceRequest lastRange = new DiscoverDeviceRequest(discoverDeviceRequest);
		lastRange.setDeviceStartIp(StringUtils.join(start,"."));
		lastRange.setDeviceEndIp(StringUtils.join(end,"."));
		newRanges.add(lastRange);
		discoverIPRangeDeviceRequests.setDiscoverIpRangeDeviceRequests(newRanges);
		return discoverIPRangeDeviceRequests;
	}
}
