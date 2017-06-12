package com.dell.isg.smi.gateway.manager;

import java.util.List;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;

public interface ISplitDiscoveryManager {

	public List<DiscoverdDeviceResponse> process(DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests) throws Exception;

}
