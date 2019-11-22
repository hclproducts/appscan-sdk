/**
 * © Copyright IBM Corporation 2016.
 * © Copyright HCL Technologies Ltd. 2017. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.app;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.http.HttpClient;
import com.hcl.appscan.sdk.http.HttpResponse;

public class CloudApplicationProvider implements IApplicationProvider, CoreConstants {

	private Map<String, String> m_applications;
	private IAuthenticationProvider m_authProvider;
	
	public CloudApplicationProvider(IAuthenticationProvider provider) {
		m_authProvider = provider;
	}
	
	@Override
	public Map<String, String> getApplications() {
		if(m_applications == null)
			loadApplications();
		return m_applications;
	}

	@Override
	public String getAppName(String id) {
		return getApplications().get(id);
	}

	private void loadApplications() {
		if(m_authProvider.isTokenExpired())
			return;
		
		m_applications = new HashMap<String, String>();
		String url =  m_authProvider.getServer() + API_APPS + "?fields=Name&sort=%2BName"; //$NON-NLS-1$
		Map<String, String> headers = m_authProvider.getAuthorizationHeader(true);
		headers.putAll(Collections.singletonMap("range", "items=0-999999")); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpClient client = new HttpClient(m_authProvider.getProxy());
		
		try {
			HttpResponse response = client.get(url, headers, null);
			
			if (!response.isSuccess())
				return;
		
			JSONArray array = (JSONArray)response.getResponseBodyAsJSON();
			if(array == null)
				return;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				String id = object.getString(ID);
				String name = object.getString(NAME);
				m_applications.put(id, name);
			}
		}
		catch(IOException | JSONException e) {
			m_applications = null;
		}
	}
}
