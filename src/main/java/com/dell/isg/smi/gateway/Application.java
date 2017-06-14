/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.dell.isg.smi.config.gateway.DiscoveryConfiguration;
import com.dell.isg.smi.gateway.filter.DiscoveryRangeRequestFilter;
import com.dell.isg.smi.service.config.InnerConfig;

@RibbonClients({
    @RibbonClient(name = "discoveryConfiguration", configuration = DiscoveryConfiguration.class)
})

@SpringCloudApplication
@EnableZuulProxy
@EnableFeignClients("com.dell.isg.smi.gateway")
@Import(InnerConfig.class)
public class Application extends SpringBootServletInitializer{

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public DiscoveryRangeRequestFilter discoveryRangeFilter() {
        return new DiscoveryRangeRequestFilter();
    }
}