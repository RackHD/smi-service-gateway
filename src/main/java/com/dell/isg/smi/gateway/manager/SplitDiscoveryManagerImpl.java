package com.dell.isg.smi.gateway.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceTypes;
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
		List<DiscoverdDeviceResponse> responseList = new ArrayList<DiscoverdDeviceResponse>();

		if (ranges.size() >= 1) {
			ExecutorService executorService = Executors.newFixedThreadPool(ranges.size());
			Set<Callable<List<DiscoverdDeviceResponse>>> callables = new HashSet<Callable<List<DiscoverdDeviceResponse>>>();
			for (DiscoverDeviceRequest discoverDeviceRequest : ranges) {
				DiscoverIPRangeDeviceRequests newDiscoverIPRangeDeviceRequests = new DiscoverIPRangeDeviceRequests();
				newDiscoverIPRangeDeviceRequests.setCredential(discoverIPRangeDeviceRequests.getCredential());
				Set<DiscoverDeviceRequest> newRanges = new HashSet<DiscoverDeviceRequest>();
				newRanges.add(discoverDeviceRequest);
				newDiscoverIPRangeDeviceRequests.setDiscoverIpRangeDeviceRequests(newRanges);
				// Future<List<DiscoverdDeviceResponse>> future =
				// executorService.submit(new
				// Callable<List<DiscoverdDeviceResponse>>(){
				// public List<DiscoverdDeviceResponse> call() throws Exception
				// {
				// return
				// discoveryClient.discover(newDiscoverIPRangeDeviceRequests);
				// }
				// });
				callables.add(new Callable<List<DiscoverdDeviceResponse>>() {
					public List<DiscoverdDeviceResponse> call() throws Exception {
						return discoveryClient.discover(newDiscoverIPRangeDeviceRequests);
					}
				});
				// aggregateResponses(responseList, future.get());
			}
			List<Future<List<DiscoverdDeviceResponse>>> futures = executorService.invokeAll(callables);

			for (Future<List<DiscoverdDeviceResponse>> future : futures) {
				aggregateResponses(responseList, future.get());
			}
			executorService.shutdown();
		}

		return responseList;
	}

	@SuppressWarnings("deprecation")
	private void aggregateResponses(List<DiscoverdDeviceResponse> responseList,
			List<DiscoverdDeviceResponse> response) {

		for (DiscoverdDeviceResponse discoverdDeviceResponse : response) {
			DiscoverdDeviceResponse aggregateDiscoverdDeviceResponse = CollectionUtils.find(responseList,
					predicateDiscoverdDeviceResponse(discoverdDeviceResponse.getDeviceGroup()));
			if (aggregateDiscoverdDeviceResponse == null) {
				responseList.add(discoverdDeviceResponse);
			} else {
				List<DiscoveredDeviceTypes> discoveredDeviceTypes = discoverdDeviceResponse.getDiscoveredDeviceList();
				for (DiscoveredDeviceTypes discoveredDeviceType : discoveredDeviceTypes) {
					DiscoveredDeviceTypes aggregateDiscoveredDeviceTypes = CollectionUtils.find(
							aggregateDiscoverdDeviceResponse.getDiscoveredDeviceList(),
							predicateDiscoveredDeviceTypes(discoveredDeviceType.getDeviceName()));
					if (aggregateDiscoveredDeviceTypes == null) {
						aggregateDiscoverdDeviceResponse.getDiscoveredDeviceList().add(discoveredDeviceType);
					} else {
						aggregateDiscoveredDeviceTypes.getDiscoveredDeviceInfoList()
								.addAll(discoveredDeviceType.getDiscoveredDeviceInfoList());
						aggregateDiscoveredDeviceTypes
								.setDiscovered(aggregateDiscoveredDeviceTypes.getDiscoveredDeviceInfoList().size());
						aggregateDiscoverdDeviceResponse.getDiscoveredDeviceList().add(aggregateDiscoveredDeviceTypes);
					}
					responseList.add(aggregateDiscoverdDeviceResponse);
				}
			}
		}

	}

	private Predicate<DiscoverdDeviceResponse> predicateDiscoverdDeviceResponse(String groupName) {
		return new Predicate<DiscoverdDeviceResponse>() {
			@Override
			public boolean evaluate(DiscoverdDeviceResponse discoverdDeviceResponse) {
				if (discoverdDeviceResponse == null) {
					return false;
				}
				return discoverdDeviceResponse.getDeviceGroup().equals(groupName);
			}
		};
	}

	private Predicate<DiscoveredDeviceTypes> predicateDiscoveredDeviceTypes(String deviceName) {
		return new Predicate<DiscoveredDeviceTypes>() {
			@Override
			public boolean evaluate(DiscoveredDeviceTypes discoveredDeviceTypes) {
				if (discoveredDeviceTypes == null) {
					return false;
				}
				return discoveredDeviceTypes.getDeviceName().equals(deviceName);
			}
		};
	}

}
