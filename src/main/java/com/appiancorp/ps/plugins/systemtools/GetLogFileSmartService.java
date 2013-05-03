package com.appiancorp.ps.plugins.systemtools;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.cfg.Configuration;
import com.appiancorp.suiteapi.cfg.ConfigurationLoader;
import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentOutputStream;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.appiancorp.suiteapi.knowledge.FolderDataType;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Order;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;

@PaletteInfo(paletteCategory="Appian Smart Services", palette="System Tools")
@Order({Constants.ACP_LOG_FILES, Constants.ACP_FOLDER_ID})
public class GetLogFileSmartService extends AppianSmartService {

	private static final Logger LOG = Logger.getLogger(GetLogFileSmartService.class);
	private ContentService cs;

	//Inputs
	private String[] logFiles;
	private Long folderId;
	private String serverLogPath = null;

	//Outputs
	private Long[] newDocuments;


	public GetLogFileSmartService(ContentService cs) {
		super();
		this.cs = cs;

		Configuration config = ConfigurationLoader.getConfiguration();
		this.serverLogPath = config.getAeLogs();
	}

	@Override
	public void validate(MessageContainer messages) {

		if(this.logFiles == null) {
			messages.addError(Constants.ACP_LOG_FILES, Constants.LOG_FILES_REQUIRED);
			LOG.warn("Log file is required");
		}

		if(this.folderId == null) {
			messages.addError(Constants.ACP_FOLDER_ID, Constants.FOLDER_ID_REQUIRED);
			LOG.warn("Folder Id is required.");
		}

		for(String logFile : logFiles) {

			if(StringUtils.isBlank(logFile)) {
				messages.addError(Constants.ACP_LOG_FILES, Constants.LOG_FILE_VALIDATION);
				LOG.warn("Log file name: " + logFile + " cannot be blank, null, or empty.");
				break;
			}

			String absolutePath;
			try {

				String fileName = this.serverLogPath + File.separatorChar + logFile;
				File file = new File(fileName);
				absolutePath = file.getCanonicalPath();

				if(!file.isFile() || file.isDirectory()) {
					messages.addError(Constants.ACP_LOG_FILES, Constants.LOG_FILE_INVALID_FILE);
					LOG.warn("Log file name: " + logFile + " must be a file and not a directory.");
					break;
				}

				if(!absolutePath.toLowerCase().startsWith(this.serverLogPath.toLowerCase())) {
					messages.addError(Constants.ACP_LOG_FILES, Constants.LOG_FILE_INVALID_LOCATION);
					LOG.warn("Log file name: " + logFile + " must be within the specified logs directory.");
				}

			} catch (IOException e) {
				messages.addError(Constants.ACP_LOG_FILES, Constants.ERROR_FILE_IO_EXCEPTION);
				LOG.warn("File " + logFile + " has an IO exception.", e);
			}
		}
	}

	@Override
	public void run() throws SmartServiceException {

		ContentOutputStream cos = null;
		BufferedReader br = null;
		String line = null;
		newDocuments = new Long[logFiles.length];

		for(int i = 0; i<logFiles.length; i++) {

			try {
				//Get File Properties
				String filePath = this.serverLogPath + File.separatorChar + logFiles[i];
				File logFile = new File(filePath);

				int extensionIndex = logFile.getName().lastIndexOf(".");
				String fileName = logFile.getName().substring(0, extensionIndex);
				String extension = logFile.getName().substring(extensionIndex+1, logFile.getName().length());

				//Create Appian Document
				Document doc = new Document();
				doc.setName(fileName);
				doc.setExtension(extension);
				doc.setParent(this.folderId);

				//Upload Appian Document
				cos = cs.upload(doc, ContentConstants.UNIQUE_NONE);
				Long newDocId = cos.getContentId();
				newDocuments[i] = newDocId;

				//Write to Appian Document
				br = new BufferedReader(new FileReader(logFile));
				while((line = br.readLine()) != null) {
					cos.write(line.getBytes());
					cos.write(System.getProperty("line.separator").getBytes());
				}

			} catch (FileNotFoundException e) {
				LOG.error("File " + logFiles[i] + " could not be accessed.", e);
				throw createException(e, Constants.ERROR_FILE_NOT_FOUND_EXCEPTION, e.getMessage());
			} catch (IOException e) {
				LOG.error("File " + logFiles[i] + " has an IO exception.", e);
				throw createException(e, Constants.ERROR_FILE_IO_EXCEPTION, e.getMessage());
			} catch (Exception e) {
				LOG.error("Runtime error: " + e.getMessage(), e);
				throw createException(e, Constants.ERROR_CREATE_LOG_FILE, e.getMessage());
			}
			finally {
				if(br != null) {
					try{
						br.close();
					} catch (IOException e) {
						LOG.warn("Failed to close BufferedReader.", e);
					}
				}
				if(cos != null) {
					try{
						cos.close();
					} catch (IOException e) {
						LOG.warn("Failed to close ContentOutputStream.", e);
					}
				}
			}
		}
	}

	private SmartServiceException createException(Throwable t, String key, Object... args) {
		return new SmartServiceException.Builder(getClass(), t).userMessage(key, args).build();
	}

	@Name(Constants.ARV_NEW_DOCUMENTS)
	@DocumentDataType
	public Long[] getNewDocuments() {
		return newDocuments;
	}

	@Name(Constants.ACP_LOG_FILES)
	public void setLogFiles(String[] logFiles) {
		this.logFiles = logFiles;
	}

	@Name(Constants.ACP_FOLDER_ID)
	@FolderDataType
	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}
}
