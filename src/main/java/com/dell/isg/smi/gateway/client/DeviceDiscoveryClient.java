/**
 * Copyright � 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.gateway.client;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.gateway.configuration.DiscoveryConfiguration;

@FeignClient(name = "DEVICE-DISCOVERY", configuration=DiscoveryConfiguration.class)
public interface DeviceDiscoveryClient {
	
	@RequestMapping(value = "/api/1.0/discover/range", method = RequestMethod.POST, headers = "Accept=application/json", consumes = "application/json", produces = "application/json")
	public @ResponseBody List<DiscoverdDeviceResponse> discover(@RequestBody DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests);

}
