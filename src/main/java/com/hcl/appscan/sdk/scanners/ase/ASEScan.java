/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scanners.ase;

import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.error.InvalidTargetException;
import com.hcl.appscan.sdk.error.ScannerException;
import com.hcl.appscan.sdk.logging.DefaultProgress;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.results.ASEResultsProvider;
import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.scan.IScanServiceProvider;
import com.hcl.appscan.sdk.scanners.ASoCScan;
import com.hcl.appscan.sdk.scanners.ScanConstants;

import java.io.Serializable;
import java.util.Map;

public class ASEScan extends ASoCScan implements ScanConstants, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //TODO : figure out the report format which ASE supports
	private static final String REPORT_FORMAT = "json"; //$NON-NLS-1$
	
        
    public ASEScan(Map<String, String> properties, IScanServiceProvider provider) {
		super(properties, new DefaultProgress(), provider);
	}
	
	public ASEScan(Map<String, String> properties, IProgress progress, IScanServiceProvider provider) {
		super(properties, progress, provider);
	}
        
    @Override
    public void run() throws ScannerException, InvalidTargetException {       
 
        String id=getServiceProvider().createAndExecuteScan(null, getProperties());        
        setScanId(id);
        if(getScanId() == null)
        	throw new ScannerException(Messages.getMessage(ERROR_CREATING_SCAN));
    }

    @Override
    public String getType() {
        return ASEConstants.ASE_DAST;
    }

    @Override
    public IResultsProvider getResultsProvider() {
        ASEResultsProvider provider = new ASEResultsProvider(getScanId(), getType(), getServiceProvider(), getProgress(), getName());
        provider.setReportFormat(getReportFormat());
        return provider;
    }

    @Override
    public String getReportFormat() {
        return REPORT_FORMAT;
    }
}