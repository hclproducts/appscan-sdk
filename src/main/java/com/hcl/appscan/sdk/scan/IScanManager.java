/**
 * © Copyright IBM Corporation 2016.
 * © Copyright HCL Technologies Ltd. 2017. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scan;

import java.io.File;
import java.util.Map;

import com.hcl.appscan.sdk.error.AppScanException;
import com.hcl.appscan.sdk.logging.IProgress;

public interface IScanManager {
	
	/**
	 * Performs any steps necessary to prepare for a scan.
	 * @param progress Tracks the progress of preparation.
	 * @param properties Additional properties to be used.
	 * @throws AppScanException If an error occurs.
	 */
	void prepare(IProgress progress, Map<String, String> properties)  throws AppScanException;
	
	/**
	 * Runs analysis.
	 * @param progress Tracks the progress of analysis.
	 * @param properties Additional properties to be used in analysis.
	 * @param provider A provider of scanning services.
	 * @throws AppScanException If an error occurs.
	 */
	void analyze(IProgress progress, Map<String, String> properties, IScanServiceProvider provider) throws AppScanException;
	
	/**
	 * Adds an item to be scanned.
	 * @param target The target to add.
	 */
	void addScanTarget(ITarget target);	
	
	/**
	 * Retrieves the scan results as a file.
	 * @param destination The destination File for the results.
	 * @param format The format of the results.
	 * @throws AppScanException if there are no results available.
	 */
	void getScanResults(File destination, String format) throws AppScanException;
}
