/**
 * © Copyright IBM Corporation 2016.
 * © Copyright HCL Technologies Ltd. 2017. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.sdk.scanners.sast.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.hcl.appscan.sdk.scanners.sast.targets.ISASTTarget;
import com.hcl.appscan.sdk.utils.SystemUtil;

public class XmlWriter extends ModelWriter 
implements	IModelXMLConstants 
{

	private DOMWriter m_config;  
	private String m_configOutputDirectory = null;
	private String m_configFileName = SystemUtil.isVector() ? VECTOR_CONFIG + DOT_XML : APPSCAN_CONFIG + DOT_XML;

	@Override
	public void initWriters(File directory) throws IOException {
		m_configOutputDirectory = directory.getCanonicalPath();
		try {
			initialize(directory);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		m_config = new DOMWriter(directory, m_configFileName, m_builder);
	}

	@Override
	public void visit(List<ISASTTarget> targets) {
		m_config.beginElement(E_CONFIGURATION);
		m_config.beginElement(E_TARGETS);

		for (ISASTTarget target: targets){
			//Add Target
			m_config.beginElement(E_TARGET);
			m_config.setAttribute(A_PATH, target.getTargetFile().getAbsolutePath());
			if(target.outputsOnly())
				m_config.setAttribute(A_OUTPUTS_ONLY, "true");
			
			//Add CustomBuildInfo
			if(target.getProperties().size() > 0) {
				m_config.beginElement(E_CUSTOM_BUILD_INFO);
				
				for (Entry<String, String> buildInfo : target.getProperties().entrySet())
					m_config.setAttribute(buildInfo.getKey(), buildInfo.getValue());

				m_config.endElement();
			}
			
			//Add Include patterns
			for(String include : target.getInclusionPatterns()) {
				m_config.beginElement(E_INCLUDE);
				m_config.endElement(include);
			}
						
			//Add Exclude patterns
			for(String exclude : target.getExclusionPatterns()) {
				m_config.beginElement(E_EXCLUDE);
				m_config.endElement(exclude);
			}
			
			m_config.endElement(); // </Target>
		}

		m_config.endElement(); // </Configuration>
	}

	@Override
	public void write() throws TransformerException {
		m_transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); //$NON-NLS-1$
		m_config.write(m_transformer);
	}

	/**
	 * Returns the location of the generated configuration file.
	 * @return The location of the generated configuration file.
	 */
	@Override
	public String getOutputLocation() {
		return m_configOutputDirectory+File.separator+m_configFileName;
	}
}
