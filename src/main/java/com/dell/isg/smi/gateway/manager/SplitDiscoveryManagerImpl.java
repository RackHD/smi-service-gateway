package com.dell.isg.smi.gateway.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.gateway.client.DeviceDiscoveryClient;

@Component
public class SplitDiscoveryManagerImpl implements ISplitDiscoveryManager{
	
	@Autowired
	private DeviceDiscoveryClient discoveryClient;

	@Override
	public List<DiscoverdDeviceResponse> process(DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests)
			throws Exception {
		Set<DiscoverDeviceRequest> ranges = discoverIPRangeDeviceRequests.getDiscoverIpRangeDeviceRequests();
		if (ranges.size() >= 1) {
			for (DiscoverDeviceRequest discoverDeviceRequest : ranges) {
				DiscoverIPRangeDeviceRequests newDiscoverIPRangeDeviceRequests = new DiscoverIPRangeDeviceRequests();
				newDiscoverIPRangeDeviceRequests.setCredential(discoverIPRangeDeviceRequests.getCredential());
				Set<DiscoverDeviceRequest> newRanges = new HashSet<DiscoverDeviceRequest>();
				newRanges.add(discoverDeviceRequest);
				newDiscoverIPRangeDeviceRequests.setDiscoverIpRangeDeviceRequests(newRanges);
				discoveryClient.discover(newDiscoverIPRangeDeviceRequests);
			}
		}
		return null;
	}

}
