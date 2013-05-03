package com.appiancorp.ps.plugins.systemtools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


import com.appiancorp.services.ServiceContext;
import com.appiancorp.suiteapi.cfg.Configuration;
import com.appiancorp.suiteapi.cfg.ConfigurationLoader;
import com.appiancorp.suiteapi.expression.annotations.AppianScriptingFunctionsCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;

@AppianScriptingFunctionsCategory
public class SystemToolsFunctions {

	private static final Logger LOG = Logger.getLogger(SystemToolsFunctions.class);
	
	/**
	 * This function returns a list of available server log file names from
	 * the file system
	 * 
	 * @param sc
	 * @return
	 */
	@Function
	public String[] listLogFiles(ServiceContext sc) {
		
		Configuration config = ConfigurationLoader.getConfiguration();
		String logPath = config.getAeLogs();
		
		if(logPath == null) {
			LOG.warn("The logs directory does not exist.");
			return null;
		}
		
		List<String> filesList = traverseFiles(logPath, logPath, new ArrayList<String>());
		String[] filesArray = new String[filesList.size()];
		filesArray = filesList.toArray(filesArray);

		return filesArray;
	}
	

	/**
	 * Recursively traverse through the directories and find all files.
	 * 
	 * @param aeLogPath
	 * @param absolutePath
	 * @param files
	 * @return
	 */
	private List<String> traverseFiles(String aeLogPath, String path, List<String> files) {
		
		File root = new File(path);
		File[] logFiles = root.listFiles();
		
		for(File file : logFiles) {
			if(file.isDirectory()) {
				traverseFiles(aeLogPath, file.getAbsolutePath(), files);
			} else {
				
				StringBuilder filePathBuilder = new StringBuilder();
				
				//find relative path starting from the AE log path directory
				if(!aeLogPath.equals(path)) {
					filePathBuilder.append(path.substring(aeLogPath.length() + 1, path.length()));
					filePathBuilder.append(File.separatorChar);
				}

				filePathBuilder.append(file.getName());
				files.add(filePathBuilder.toString());
			}
		}
		return files;
	}
}
