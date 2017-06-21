/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.config.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ConfigurationBasedServerList;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import feign.Feign;
import feign.Logger;
import feign.Request;

@Configuration
public class DiscoveryConfiguration {
	
	private String name = "discoveryConfiguration";
	
	@Bean
	@Scope("prototype")
	public Feign.Builder feignBuilder() {
		return Feign.builder();
	}

	public static final int EIGHT_MINUTE = 1000 * 60 * 8;

	@Bean
	public Logger.Level feignLogger() {
		return Logger.Level.FULL;
	}

	@Bean
	public Request.Options options() {
		return new Request.Options(EIGHT_MINUTE, EIGHT_MINUTE);
	}
	
	 @Bean
     @ConditionalOnMissingBean
     public IClientConfig ribbonClientConfig() {
         DefaultClientConfigImpl config = new DefaultClientConfigImpl();
         config.loadProperties(this.name);
         return config;
     }

     @Bean
     ServerList<Server> ribbonServerList(IClientConfig config) {
         ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
         serverList.initWithNiwsConfig(config);
         return serverList;
     }
     
     @Bean
     public IPing ribbonPing(IClientConfig config) {
         return new PingUrl();
     }
 	
 	@Bean
 	public IRule ribbonRule(IClientConfig config) {
 		return new RoundRobinRule();
 	}
}
