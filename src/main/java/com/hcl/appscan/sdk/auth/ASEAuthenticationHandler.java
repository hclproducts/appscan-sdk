/**
 * © Copyright HCL Technologies Ltd. 2019,2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.auth;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.error.HttpException;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.http.HttpsClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

public class ASEAuthenticationHandler implements CoreConstants{
    private IASEAuthenticationProvider m_authProvider;
    private List<String> cookies ;
    
    public ASEAuthenticationHandler(IASEAuthenticationProvider provider) {
		m_authProvider = provider;
	}

	/**
	 * Authenticates a user using the given LoginType.
	 * @param username The username.
	 * @param password The password.
	 * @param persist True to persist the credentials.
	 * @param url The url of the ASE server
	 * @return True if successful.
	 * @throws IOException If an error occurs.
	 * @throws JSONException If an error occurs.
	 */
	public boolean login(String username, String password, boolean persist,String url) throws IOException, JSONException {
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		headers.put(CHARSET, UTF8);
		headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(ASE_KEY_ID, username);
		params.put(ASE_KEY_SECRET, password);
		url=url+"/api/keylogin/apikeylogin";

		HttpsClient client = new HttpsClient();
		HttpResponse response = client.postForm(url, headers, params);
		cookies=response.getResponseHeaders().get("Set-Cookie");
	    
		if(response.getResponseCode() == HttpsURLConnection.HTTP_OK || response.getResponseCode() == HttpsURLConnection.HTTP_CREATED) {
			if(persist) {
				JSONObject object = (JSONObject)response.getResponseBodyAsJSON();
				String token = object.getString("sessionId");
				List<String> cookies =response.getResponseHeaders().get("Set-Cookie");                                       
				m_authProvider.saveConnection(token);
				m_authProvider.setCookies(cookies);
			}
			return true;
		}
		else {
			String reason = response.getResponseBodyAsString() == null ?
			Messages.getMessage("message.unknown") : response.getResponseBodyAsString(); //$NON-NLS-1$
			throw new HttpException(response.getResponseCode(), reason);
		}
	}
        
        
    public List<String> getCookies() {
        return cookies;
    }
	
	public boolean isTokenExpired() {
		boolean isExpired;
		String request_url = m_authProvider.getServer() + ASE_CURRENTUSER_V2;
		
		Map<String, String> headers = m_authProvider.getAuthorizationHeader(false);
		headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		headers.put(CHARSET, UTF8);
		headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient httpClient = new HttpsClient();
		HttpResponse httpResponse;
		try {
			httpResponse = httpClient.get(request_url, headers, null);
			isExpired = httpResponse.getResponseCode() != HttpsURLConnection.HTTP_OK;
		} catch (IOException e) {
			isExpired = true;
		}
		return isExpired;
	}
}