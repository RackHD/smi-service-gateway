/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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

@EnableZuulProxy
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.dell.isg.smi.gateway")
@EnableAutoConfiguration
@Import(InnerConfig.class)
@RefreshScope
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public DiscoveryRangeRequestFilter discoveryRangeFilter() {
        return new DiscoveryRangeRequestFilter();
    }
}