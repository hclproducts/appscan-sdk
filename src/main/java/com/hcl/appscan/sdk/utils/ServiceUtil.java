/**
 * © Copyright IBM Corporation 2016.
 * © Copyright HCL Technologies Ltd. 2017. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.utils;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;

import javax.net.ssl.HttpsURLConnection;

import org.apache.wink.json4j.JSONArtifact;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.http.HttpClient;
import com.hcl.appscan.sdk.http.HttpResponse;

/**
 * Provides scan service utilities.
 */
public class ServiceUtil implements CoreConstants {
	
	/**
	 * Gets the SAClientUtil package used for running static analysis.
	 * 
	 * @param destination The file to save the package to.
	 * @throws IOException If an error occurs.
	 */
	public static void getSAClientUtil(File destination) throws IOException {
		getSAClientUtil(destination, Proxy.NO_PROXY);
	}
	
	/**
	 * Gets the SAClientUtil package used for running static analysis.
	 * 
	 * @param destination The file to save the package to.
	 * @param proxy The proxy for the connection, if required.
	 * @throws IOException If an error occurs.
	 */
	public static void getSAClientUtil(File destination, Proxy proxy) throws IOException {
		String request_url = SystemUtil.getDefaultServer() + String.format(API_SACLIENT_DOWNLOAD, API_SCX, SystemUtil.getOS());
		
		HttpClient client = new HttpClient(proxy);
		HttpResponse response = client.get(request_url, null, null);
		
		if (response.getResponseCode() == HttpsURLConnection.HTTP_OK || response.getResponseCode() == HttpsURLConnection.HTTP_CREATED) {
			if(!destination.getParentFile().isDirectory())
				destination.getParentFile().mkdirs();
			
			response.getResponseBodyAsFile(destination);
		}
		else
			throw new IOException(response.getResponseBodyAsString());
	}
	
	/**
	 * Gets the latest available version of the SAClientUtil package used for running static analysis.
	 * 
	 * @return The current version of the package.
	 * @throws IOException If an error occurs.
	 */
	public static String getSAClientVersion() throws IOException {
		return getSAClientVersion(Proxy.NO_PROXY);
	}
	
	/**
	 * Gets the latest available version of the SAClientUtil package used for running static analysis.
	 * 
	 * @param proxy The {@link Proxy} to use.
	 * @return The current version of the package.
	 * @throws IOException If an error occurs.
	 */
	public static String getSAClientVersion(Proxy proxy) throws IOException {
		String request_url = SystemUtil.getDefaultServer() + String.format(API_SACLIENT_VERSION, API_SCX, SystemUtil.getOS(), "true"); //$NON-NLS-1$
		
		HttpClient client = new HttpClient(proxy);
		HttpResponse response = client.get(request_url, null, null);
		
		if (response.getResponseCode() == HttpsURLConnection.HTTP_OK || response.getResponseCode() == HttpsURLConnection.HTTP_CREATED) {
			try {
				JSONArtifact responseContent = response.getResponseBodyAsJSON();
				if (responseContent != null) {
					JSONObject object = (JSONObject) responseContent;
					return object.getString(VERSION_NUMBER);
				}
			} catch (JSONException e) {
				return "0"; //$NON-NLS-1$
			}
		}
		return null;
	}
}
