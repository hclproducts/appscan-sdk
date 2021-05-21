/**
 * © Copyright HCL Technologies Ltd. 2017. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scanners.sast.targets;

public interface ICppTarget extends ISASTTarget {

	/**
	 * Gets the compiler options used to build the target.
	 * @return A String representation o the compiler options.
	 */
	String getCompilerOptions();
	
	/**
	 * Gets the macros defined for the target.
	 * @return The macros as a String.
	 */
	String getMacros();
	
	/**
	 * Gets the include directories for the target.
	 * @return The include directories as a String.
	 */
	String getIncludeDirs();

	/**
	 * Gets the source files for the target.
	 * @return The source files as a String.
	 */
	String getSourceFiles();

	/**
	 * Gets the version of the target.
	 * @return The version as a String.
	 */
	String getVersion();
	
	/**
	 * Gets the platform architecture of the target.
	 * @return The platform architecture as a String.
	 */
	String getPlatformArch();
}
