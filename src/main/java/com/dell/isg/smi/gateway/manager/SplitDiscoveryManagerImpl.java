package com.dell.isg.smi.gateway.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

		if (ranges.size() >= 1) {
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
		}

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
				gatewayResponse.set(indexGroup,aggregateDiscoverdDevice);
			}

		}

	}

}
