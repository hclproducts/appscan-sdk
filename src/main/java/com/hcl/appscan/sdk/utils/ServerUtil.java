package com.hcl.appscan.sdk.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.http.HttpClient;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.logging.StdOutProgress;


public class ServerUtil {

	private static final String DEFAULT = "default"; //$NON-NLS-1$
	private static String CURRENT_SERVER = getConfiguredServer();
	private static Map<String, String> m_servers = null;
	
	/**
	 * Gets the target server. 
	 * 
	 * @return The service url.
	 */
	public static String getServerUrl() {
		return CURRENT_SERVER;
	}
	
	/**
	 * Given a region, returns the service url for that region. If the region does not exist, the url for the default region is returned.
	 * 
	 * @param region The region as a String.
	 * @return The service url for the given region or the default service url if the region is not known.
	 */
	public static String getServerUrl(String region) {
		if(m_servers == null) {
			initServerMap();
		}
		
		if(region != null && m_servers.containsKey(region))
			CURRENT_SERVER = m_servers.get(region);
		else
			CURRENT_SERVER = m_servers.get(DEFAULT);
		
		return CURRENT_SERVER;
	}
	
	private static void initServerMap() {
		m_servers = new HashMap<String, String>();
		m_servers.put(DEFAULT, getConfiguredServer());
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CoreConstants.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
		String url = getConfiguredServer() + CoreConstants.API_REGIONS;
		HttpClient client = new HttpClient();
		HttpResponse response = null;
		
		try {
			response = client.get(url, headers, null);
			if(response.isSuccess()) {
				JSONObject obj = (JSONObject)response.getResponseBodyAsJSON();
				if(obj == null)
					return;
				
				JSONArray regions = obj.getJSONArray(CoreConstants.REGIONS);
				for (int i = 0; i < regions.size(); i++) {
					JSONObject region = (JSONObject) regions.get(i);
					m_servers.put(region.getString(CoreConstants.ID), region.getString(CoreConstants.URL));
				}
				
				String defaultRegion = obj.getString(CoreConstants.DEFAULT_REGION);
				m_servers.put(DEFAULT, m_servers.get(defaultRegion));
			}
		} catch (IOException | JSONException e) {
			new StdOutProgress().setStatus(e);
		}
	}
	
	/**
	 * Gets the server as configured by the user via the -DBLUEMIX_SERVER JVM parameter. If this is not set, returns the default HCL server https://cloud.appscan.com
	 * 
	 * @return The server configured by the user or https://cloud.appscan.com.
	 */
	private static String getConfiguredServer() {
		SystemUtil.setSystemProperties();
		String server = System.getProperty("BLUEMIX_SERVER"); //$NON-NLS-1$
		return server != null ? server : CoreConstants.DEFAULT_SERVER;
	}
}
