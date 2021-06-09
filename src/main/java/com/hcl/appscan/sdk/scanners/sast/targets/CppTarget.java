/**
 * © Copyright HCL Technologies Ltd. 2017. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scanners.sast.targets;

import java.util.HashMap;
import java.util.Map;

import com.hcl.appscan.sdk.scanners.sast.xml.IModelXMLConstants;

public abstract class CppTarget extends DefaultTarget implements ICppTarget {

	@Override
	public Map<String, String> getProperties() {
		HashMap<String, String> buildInfos = new HashMap<String, String>();
		buildInfos.put(IModelXMLConstants.A_COMPILER_OPTS, getCompilerOptions());
		buildInfos.put(IModelXMLConstants.A_MACROS, getMacros());
		buildInfos.put(IModelXMLConstants.A_INCLUDE_PATHS, getIncludeDirs());
		buildInfos.put(IModelXMLConstants.A_SOURCES, getSourceFiles());
		buildInfos.put(IModelXMLConstants.A_VS_VERSION, getVersion());
		buildInfos.put(IModelXMLConstants.A_PLATFORM_ARCH, getPlatformArch());
		buildInfos.put(IModelXMLConstants.A_INTERMEDIATE_OUTPUT_PATH, getIntermediateOutputPath());
		return buildInfos;
	}
}
