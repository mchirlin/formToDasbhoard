package com.appiancorp.ps.plugins.systemtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.appiancorp.ps.plugins.systemtools.ProcessModelSettings;
import com.appiancorp.suiteapi.cfg.ConfigurationLoader;
import com.appiancorp.suiteapi.collaboration.FolderService;
import com.appiancorp.suiteapi.common.LocalObject;
import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.common.RoleMap;
import com.appiancorp.suiteapi.content.Content;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.personalization.GroupService;
import com.appiancorp.suiteapi.portal.GlobalizationService;
import com.appiancorp.suiteapi.process.AbstractProcessModel;
import com.appiancorp.suiteapi.process.ProcessDesignService;
import com.appiancorp.suiteapi.process.ProcessModel;
import com.appiancorp.suiteapi.process.ProcessModel.Descriptor;
import com.appiancorp.suiteapi.process.ProcessModelFolder;
import com.appiancorp.suiteapi.process.ProcessModelNotificationSettings;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Order;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;


@PaletteInfo(paletteCategory="Appian Smart Services", palette="System Tools")
@Order({"ProcessModels"})
public class GetProcessModelsSettings extends AppianSmartService {

  private static final Logger LOG = Logger.getLogger(GetProcessModelsSettings.class);

  private final SmartServiceContext smartServiceCtx;
  private final GlobalizationService gls;
  private final ProcessDesignService pds;
  private final GroupService gs;
  private final ContentService cs;
  
  private final Integer defaultArchivalDays = ConfigurationLoader.getConfiguration().getAutoArchiveDelay();
  
  private ProcessModelSettings[] processModels;
 
  private String errorMessage;

  public GetProcessModelsSettings(SmartServiceContext smartServiceCtx, ProcessDesignService pds, GlobalizationService gls, GroupService gs, ContentService cs) {
    super();
    this.smartServiceCtx = smartServiceCtx;
    this.gls = gls;
    this.pds = pds;
    this.gs = gs;
    this.cs = cs;
  }
  
  @Override
  public void run() throws SmartServiceException {
	  Descriptor[] pmds = pds.getProcessModelsICanStart();
	  
	  try {
		  List<ProcessModelSettings> processModelList = new ArrayList<ProcessModelSettings>();
 
		  for (Descriptor pmd : pmds) {
			  Long id = pmd.getId();
			  String uuid = pmd.getUuid();
			  String name = pmd.getName().get(gls.getSiteLocaleSettings().getPrimaryLocale());
			  String folderStructure = "";
			  
			  ProcessModel pm = pds.getProcessModel(pmd.getId());

			  ProcessModelFolder pmf = pds.getFolder(pm.getFolderId());
			  String[] folderNames = pmf.getAncestorFolderNames();
			  			  
			  for (int i = 0; i < folderNames.length; i++) {
				  folderStructure += folderNames[i] + ">";
		      }
			  folderStructure += pmf.getName();
			 
			  // Calculate Security Settings
			  RoleMap rm = pds.getSecurityForProcessModel(pmd.getId()).getNative();
			  Iterator entries = rm.entrySet().iterator();
			   
			  String[] adminUsersAndGroups = new String[0];
			  String[] editorUsersAndGroups = new String[0];;
			  String[] managerUsersAndGroups = new String[0];
			  String[] viewerUsersAndGroups = new String[0];
			  String[] initiatorUsersAndGroups = new String[0];
			  String[] denyUsersAndGroups = new String[0];
			 
			  while (entries.hasNext()) {
				  Map.Entry entry = (Map.Entry) entries.next();
				  String permissionLevel = (String) entry.getKey();
				  HashMap roleMap = (HashMap) entry.getValue();
				  
				  String[] users = (String[]) roleMap.get("users");
				  Long[] groupIds = (Long[]) roleMap.get("groups");
				  
				  String[] groups = new String[groupIds.length];
				  for (int i = 0; i < groupIds.length; i++) {
					  groups[i] = gs.getGroupName(groupIds[i]);
				  }
				  
				  List<Object> holder = new ArrayList<Object>();
				  Collections.addAll(holder, users);
				  Collections.addAll(holder, groups);
				  String[] usersAndGroups = holder.toArray(new String[0]);
				  
				  if (permissionLevel.equals("ADMIN_OWNER")) {
					  adminUsersAndGroups = usersAndGroups;
				  } else if (permissionLevel.equals("EDITOR")) {
					  editorUsersAndGroups = usersAndGroups;
				  } else if (permissionLevel.equals("MANAGER")) {
					  managerUsersAndGroups = usersAndGroups;
				  } else if (permissionLevel.equals("VIEWER")) {
					  viewerUsersAndGroups = usersAndGroups;
				  } else if (permissionLevel.equals("INITIATOR")) {
					  initiatorUsersAndGroups = usersAndGroups;
				  } else if (permissionLevel.equals("EXPLICIT_NONMEMBER")) {
					  denyUsersAndGroups = usersAndGroups;
				  }
			  }
			  
			  // Get Cleanup Settings
			  Integer archiveInt = pm.getCleanupAction();
			  String cleanupType = "";
			  Integer archiveDelay = 0;
			  if (archiveInt == AbstractProcessModel.AUTO_ARCHIVE) {
				  cleanupType = "Archive";
				  archiveDelay = pm.getAutoArchiveDelay();
			  } else if (archiveInt == AbstractProcessModel.AUTO_DELETE) {
				  cleanupType = "Delete";
				  archiveDelay = pm.getAutoDeleteDelay();
			  } else if (archiveInt == AbstractProcessModel.NO_CLEANUP) {
				  cleanupType = "No Cleanup";
			  } else if (archiveInt == AbstractProcessModel.SYSTEM_DEFAULT) {
				  cleanupType = "System Default";
				  archiveDelay = defaultArchivalDays;
			  } 
			  
			  // Get Alert Settings
			  ProcessModelNotificationSettings ntfs = pm.getNtfSettings();
			  List<String> alertList = new ArrayList<String>();			  
			  if (!ntfs.isCustomSettings()) {
				  alertList.add("Use system defaults for error alert");
			  }
			  
			  if (ntfs.isNotifyInitiator()){
				  alertList.add("Send alert to the process initiator");
			  }
			  
			  if (ntfs.isNotifyOwner()) {
				  alertList.add("Send alert to the process model owner: " + pm.getOwnerUsername());
			  }
			  
			  if (ntfs.isNotifyRecipientsInExpression()) {
				  alertList.add("Send alert to the recipients defined by this expression: " + ntfs.getRecipientsExpression());
			  }
			  
			  if (ntfs.isNotifyUsersAndGroups()) {
				  String alert = "Send alert to the following users & groups: ";
				  
				  LocalObject[] usersAndGroupsObj = ntfs.getUsersAndGroups();
				  String[] alertUserGroups = new String[usersAndGroupsObj.length];
				  for (int i = 0; i < usersAndGroupsObj.length; i++) {
					  if (usersAndGroupsObj[i].getType() == 4096) {
						  alertUserGroups[i] = gs.getGroupName(usersAndGroupsObj[i].getId());
					  } else if (usersAndGroupsObj[i].getType() == 4097){
						  alertUserGroups[i] = usersAndGroupsObj[i].getStringId();
					  }
				  }
				  for (int i = 0; i < alertUserGroups.length; i++) {
					  alert += (alertUserGroups[i]);
			          if (i < alertUserGroups.length - 1) {
			        	  alert += ", ";
			          }
			      }
				  alertList.add(alert);
			  }
			  String[] alerts = new String[alertList.size()];
			  alerts = alertList.toArray(alerts);
			  			  
			  ProcessModelSettings processModel = new ProcessModelSettings(id, uuid, name, folderStructure, adminUsersAndGroups, editorUsersAndGroups, managerUsersAndGroups, viewerUsersAndGroups, initiatorUsersAndGroups, denyUsersAndGroups, cleanupType, archiveDelay, alerts);
			  processModelList.add(processModel);
		  }
		  
		  processModels = new ProcessModelSettings[processModelList.size()];
		  processModels = processModelList.toArray(processModels);
		  
	  } catch (Exception e) {
	        errorMessage = "error.generic";
	        throw createException(e, errorMessage);
	  }
  }

  public void onSave(MessageContainer messages) {
  }

  public void validate(MessageContainer messages) {
  }

  @Name("ProcessModels")
  public ProcessModelSettings[] getProcessModels() {
    return processModels;
  }
  
  private SmartServiceException createException(Throwable t, String key, Object... args) { 
    return new SmartServiceException.Builder(getClass(), t).userMessage(key, args).build(); 
  }
}
