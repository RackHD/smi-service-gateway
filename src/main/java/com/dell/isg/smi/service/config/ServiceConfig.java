/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.dell.isg.smi.service.config.ServiceConfig.Service;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@ConfigurationProperties(prefix = "smi", ignoreUnknownFields = false)
public class ServiceConfig {

    private String gateway;
    private List<Service> service = new ArrayList<Service>();


    public String getGateway() {
        return gateway;
    }


    public void setGateway(String gateway) {
        this.gateway = gateway;
    }


    public List<Service> getService() {
        return service;
    }


    public void setService(List<Service> service) {
        this.service = service;
    }


    public Object getSmiServiConfig() {
        SmiServiceConfig smi = new SmiServiceConfig();
        smi.gateway = this.gateway;
        smi.service = this.service;
        return smi;
    }

    public static class Service {

        private List<Endpoint> endpoint = new ArrayList<Endpoint>();

        private String name;


        public List<Endpoint> getEndpoint() {
            return endpoint;
        }


        public void setEndpoint(List<Endpoint> endpoint) {
            this.endpoint = endpoint;
        }


        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public static class Endpoint {

            private String url;

            private String name;


            public String getUrl() {
                return url;
            }


            public void setUrl(String url) {
                this.url = url;
            }


            public String getName() {
                return name;
            }


            public void setName(String name) {
                this.name = name;
            }


            @Override
            public String toString() {
                return ToStringBuilder.reflectionToString(this);
            }
        }
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Generated("org.jsonschema2pojo")
    @JsonPropertyOrder({ "gateway", "service" })
    class SmiServiceConfig {

        @JsonProperty("gateway")
        private String gateway;
        @JsonProperty("service")
        private List<Service> service = new ArrayList<Service>();


        /**
         * 
         * @return The gateway
         */
        @JsonProperty("gateway")
        public String getGateway() {
            return gateway;
        }


        /**
         * 
         * @param gateway The gateway
         */
        @JsonProperty("gateway")
        public void setGateway(String gateway) {
            this.gateway = gateway;
        }


        /**
         * 
         * @return The service
         */
        @JsonProperty("service")
        public List<Service> getService() {
            return service;
        }


        /**
         * 
         * @param service The service
         */
        @JsonProperty("service")
        public void setService(List<Service> service) {
            this.service = service;
        }


        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }


        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(gateway).append(service).toHashCode();
        }


        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if ((other instanceof SmiServiceConfig) == false) {
                return false;
            }
            SmiServiceConfig rhs = ((SmiServiceConfig) other);
            return new EqualsBuilder().append(gateway, rhs.gateway).append(service, rhs.service).isEquals();
        }

    }
}
