/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dell.isg.smi.service.config.ServiceConfig;

@RestController
@RequestMapping("/api/smi")
public class GatewayController {

    @Autowired
    ServiceConfig serviceConfig;


    @RequestMapping(value = "/uri", method = RequestMethod.GET, produces = "application/json")
    public Object serviceConfig() {
        return serviceConfig.getSmiServiConfig();
    }

}
