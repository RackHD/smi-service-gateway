package com.dell.isg.smi.gateway.filter;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dell.isg.smi.commons.model.device.discovery.DiscoverDeviceRequest;
import com.dell.isg.smi.commons.model.device.discovery.DiscoverIPRangeDeviceRequests;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class DiscoveryRangeRequestFilter extends ZuulFilter {

	private String URI = "api/1.0/discover/range";
	private String REDIRECT_PATH = "/api/1.0/gateway/discover/range/split";
	private String REDIRECT_URI = "http://%s:%s";

	public String filterType() {
		return "route";
	}

	public int filterOrder() {
		return 0;
	}

	public boolean shouldFilter() {
		RequestContext context = getCurrentContext();
		return StringUtils.contains(context.getRequest().getRequestURL(), URI);
	}

	public Object run() {
		try {
			RequestContext context = getCurrentContext();
			HttpServletRequest request = context.getRequest();
			String body = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
			ObjectMapper mapper = new ObjectMapper();
			DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests = mapper.readValue(body,
					DiscoverIPRangeDeviceRequests.class);
			if (doRangeSplit(discoverIPRangeDeviceRequests.getDiscoverIpRangeDeviceRequests())) {
				String url = ServletUriComponentsBuilder.fromCurrentRequest().replacePath(REDIRECT_PATH).build().toUriString();
				System.out.println("Redirect URL :" + url);
				context.setRouteHost(new URL(url));
				context.set("requestURI", "");
			}

		} catch (IOException e) {
			rethrowRuntimeException(e);
		}
		return null;
	}

	private boolean doRangeSplit(Set<DiscoverDeviceRequest> ranges) {
		boolean doSplit = false;
		if (ranges.size() > 1) {
			doSplit = true;
		} else if (ranges.size() == 1) {
			DiscoverDeviceRequest discoverDeviceRequest = ranges.iterator().next();
			doSplit = !validateIpSameSubnet(discoverDeviceRequest.getDeviceStartIp(),
					discoverDeviceRequest.getDeviceEndIp());
		}

		return doSplit;
	}

	private boolean validateIpSameSubnet(String ip1, String ip2) {
		boolean isValid = true;
		if ((ip1 != null && !ip1.isEmpty()) && (ip2 != null && !ip2.isEmpty())) {
			if (!ip1.substring(0, ip1.lastIndexOf(".")).equalsIgnoreCase(ip2.substring(0, ip2.lastIndexOf(".")))) {
				isValid = false;
			}
		}
		return isValid;
	}
}
