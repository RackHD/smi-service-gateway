/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.gateway.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverdDeviceResponse;
import com.dell.isg.smi.gateway.manager.ISplitDiscoveryManager;


@RestController
@RequestMapping("/api/1.0/gateway/discover/range")
public class DiscoveryController {

	@Autowired
	ISplitDiscoveryManager splitDiscoveryManager;
	
	@RequestMapping(value = "/split", method = RequestMethod.POST)
	public List<DiscoverdDeviceResponse> discover(@RequestBody DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests) throws Exception{
		return splitDiscoveryManager.process(discoverIPRangeDeviceRequests);
	}
}
