/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.results;

import static com.hcl.appscan.sdk.CoreConstants.API_DOWNLOAD_REPORT;
import static com.hcl.appscan.sdk.CoreConstants.API_REPORT_SELECTED_ISSUES;
import static com.hcl.appscan.sdk.CoreConstants.CONTENT_LENGTH;
import static com.hcl.appscan.sdk.CoreConstants.DEFAULT_RESULT_NAME;
import static com.hcl.appscan.sdk.CoreConstants.ERROR_GETTING_DETAILS;
import static com.hcl.appscan.sdk.CoreConstants.ERROR_GETTING_RESULT;
import static com.hcl.appscan.sdk.CoreConstants.ERROR_LOGIN_EXPIRED;
import static com.hcl.appscan.sdk.CoreConstants.FAILED;
import static com.hcl.appscan.sdk.CoreConstants.LATEST_EXECUTION;
import static com.hcl.appscan.sdk.CoreConstants.MESSAGE;
import static com.hcl.appscan.sdk.CoreConstants.RUNNING;
import static com.hcl.appscan.sdk.CoreConstants.STATUS;
import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.http.HttpClient;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
import com.hcl.appscan.sdk.scan.IScanServiceProvider;
import com.hcl.appscan.sdk.utils.SystemUtil;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 *
 * @author anurag-s
 */
public class NonCompliantIssuesProvider extends CloudResultsProvider{
    private String m_scanId;
    private IScanServiceProvider m_scanProvider;
    private IProgress m_progress;
    private String m_type;
    
    public NonCompliantIssuesProvider(String scanId, String type, IScanServiceProvider provider, IProgress progress) {
        super(scanId, type, provider, progress);
        m_scanId=scanId;
        m_scanProvider=provider;
        m_progress=progress;
        m_type=type;
    }
    
    public void getResultsFile(File file, String format) {
		if(format == null)
			format = getResultsFormat();
		
		if(file != null && !file.exists()) {
			try {
				getNonCompliantIssuesReport(m_scanId, format, file);
			} catch (IOException | JSONException e) {
				m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_GETTING_RESULT)), e);
			}
		}
	}
    
    private void loadResults() {
		try {
			JSONObject obj = m_scanProvider.getScanDetails(m_scanId);
			obj = (JSONObject) obj.get(LATEST_EXECUTION);
			JSONArray array= m_scanProvider.getNonCompliantIssues(m_scanId);
			m_status = obj.getString(STATUS);
			if(m_status != null && !m_status.equalsIgnoreCase(RUNNING)) {
				m_totalFindings = array.length();
				for (int i=0;i<array.length();i++) {
					JSONObject jobj=array.getJSONObject(i);
					String sev=jobj.getString("Severity");
					switch (sev) {
					case "High":
						m_highFindings++;
						break;
					case "Medium":
						m_mediumFindings++;
						break;
					case "Low":
						m_lowFindings++;
						break;
					case "Info":
						m_infoFindings++;
						break;
						
					default:
						break;
					}
				}
				m_hasResults = true;
			}
		} catch (IOException | JSONException | NullPointerException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_GETTING_DETAILS, e.getMessage())), e);
			m_status = FAILED;
		}
	
	}
    
    private void getNonCompliantIssuesReport(String scanId, String format, File destination) throws IOException, JSONException {
		
                String reportId=createNonCompliantIssuesReport(scanId,format);
                HttpResponse response=downloadNonCompliantIssuesReport(reportId);
                if (destination.isDirectory()) {
			String fileName = DEFAULT_RESULT_NAME + "_" + SystemUtil.getTimeStamp() + "." + format; //$NON-NLS-1$ //$NON-NLS-2$
			destination = new File(destination, fileName);
		}
	
			destination.getParentFile().mkdirs();
			response.getResponseBodyAsFile(destination);
		 
	}
        
        private HttpResponse downloadNonCompliantIssuesReport(String reportId) throws IOException, JSONException{
            IAuthenticationProvider authProvider = m_scanProvider.getAuthenticationProvider();
		if(authProvider.isTokenExpired()) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_LOGIN_EXPIRED)));
			return null;
		}
                String request_url=authProvider.getServer() + String.format(API_DOWNLOAD_REPORT,reportId);
                Map<String, String> request_headers = authProvider.getAuthorizationHeader(true);
		request_headers.put(CONTENT_LENGTH, "0"); //$NON-NLS-1$
                
                HttpClient client = new HttpClient();
		HttpResponse response = client.get(request_url, request_headers, null);
                int responseCode=response.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK)
                    return response;
                else if (responseCode == HttpsURLConnection.HTTP_INTERNAL_ERROR)
                    return downloadNonCompliantIssuesReport(reportId);
                else {
                    JSONObject object = (JSONObject) response.getResponseBodyAsJSON();
			if (object.has(MESSAGE)) {
				if (response.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST)
					m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_GETTING_RESULT)));
				else
					m_progress.setStatus(new Message(Message.ERROR, object.getString(MESSAGE)));
			}
                        return null;
                }
        }
        // post request to create the report for selected issues (non compliant), it will return the report id
        private String createNonCompliantIssuesReport(String scanId, String format) throws IOException, JSONException{
            IAuthenticationProvider authProvider = m_scanProvider.getAuthenticationProvider();
		if(authProvider.isTokenExpired()) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_LOGIN_EXPIRED)));
			return null;
		}
	
		String request_url = authProvider.getServer() + String.format(API_REPORT_SELECTED_ISSUES,"Scan", scanId);
		Map<String, String> request_headers = authProvider.getAuthorizationHeader(true);
                request_headers.put("Content-Type", "application/json; charset=UTF-8");
                request_headers.put("Accept", "application/json");
		//request_headers.put(CONTENT_LENGTH, "0"); //$NON-NLS-1$
	
		HttpClient client = new HttpClient();
		HttpResponse response = client.post(request_url, request_headers, getBodyParams(format).toString());
		if (response.getResponseCode()!=HttpsURLConnection.HTTP_OK) {
			return null;
		}
                JSONObject obj=(JSONObject)response.getResponseBodyAsJSON();
		String reportId=obj.getString("Id");
                return reportId;
        }
        private JSONObject getBodyParams(String format) throws JSONException,UnsupportedEncodingException {
		JSONObject body=new JSONObject();
                body.put("Configuration", getConfiguration(format));
		body.put("ApplyPolicies", "All");
		return body;
	}
        private JSONObject getConfiguration(String format) throws JSONException {
		JSONObject configParams=new JSONObject();
		configParams.put("Summary",true);
		configParams.put("Details", true);
		configParams.put("Discussion", false);
		configParams.put("Overview", true);
		configParams.put("TableOfContent", true);
		configParams.put("Advisories", true);
		configParams.put("FixRecommendation", true);
		configParams.put("History", true);
		configParams.put("IsTrialReport", false);
		configParams.put("ReportFileType", format);
		configParams.put("Title", getScanName());
		configParams.put("Notes", "");
		configParams.put("Locale", Locale.getDefault().toString());
		return configParams;
	}
        
        private String getScanName() {
		JSONObject obj;
		try {
			obj = m_scanProvider.getScanDetails(m_scanId);
			return obj.getString("Name");
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
