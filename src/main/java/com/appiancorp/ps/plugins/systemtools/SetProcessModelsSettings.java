package com.appiancorp.ps.plugins.systemtools;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.cfg.ConfigurationLoader;
import com.appiancorp.suiteapi.common.LocalObject;
import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.common.RoleMap;
import com.appiancorp.suiteapi.common.Security;
import com.appiancorp.suiteapi.common.exceptions.InvalidProcessModelException;
import com.appiancorp.suiteapi.personalization.GroupService;
import com.appiancorp.suiteapi.personalization.UserOrGroupDataType;
import com.appiancorp.suiteapi.portal.GlobalizationService;
import com.appiancorp.suiteapi.process.AbstractProcessModel;
import com.appiancorp.suiteapi.process.ProcessDesignService;
import com.appiancorp.suiteapi.process.ProcessModel;
import com.appiancorp.suiteapi.process.ProcessModelNotificationSettings;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Order;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;


@PaletteInfo(paletteCategory="Appian Smart Services", palette="System Tools")
@Order({"ProcessModelIds", "UpdateSecurity", "AdminUsersAndGroups", "EditorUsersAndGroups", "ManagerUsersAndGroups", "ViewerUsersAndGroups", "InitiatorUsersAndGroups", "UpdateCleanup", "CleanupType", "CleanupDelay", "UpdateAlerts", "IsCustomSetting", "IsNotifyInitiator", "IsNotifyOwner", "IsNotifyRecipientsInExpression", "NotifyRecipientsInExpression", "IsNotifyUsersAndGroups", "NotifyUsersAndGroups"})
public class SetProcessModelsSettings extends AppianSmartService {

  private static final Logger LOG = Logger.getLogger(SetProcessModelsSettings.class);

  private final SmartServiceContext smartServiceCtx;
  private final GlobalizationService gls;
  private final ProcessDesignService pds;
  private final GroupService gs;

  private final Integer defaultArchivalDays = ConfigurationLoader.getConfiguration().getAutoArchiveDelay();

  private Long[] processModelIds;

  private Boolean updateSecurity;
  private Object[] adminUsersAndGroups;
  private Object[] editorUsersAndGroups;
  private Object[] managerUsersAndGroups;
  private Object[] viewerUsersAndGroups;
  private Object[] initiatorUsersAndGroups;
  private Object[] denyUsersAndGroups;

  private Boolean updateCleanup;
  private Integer cleanupType;
  private Integer cleanupDelay;

  private Boolean updateAlerts;
  private Boolean isCustomSetting;
  private Boolean isNotifyInitiator;
  private Boolean isNotifyOwner;
  private Boolean isNotifyRecipientsInExpression;
  private String notifyRecipientsInExpression;
  private Boolean isNotifyUsersAndGroups;
  private Object[] notifyUsersAndGroups;

  private String errorMessage;

  @Override
  public void run() throws SmartServiceException {

	  try {
		  for (Long pmId: processModelIds) {
			  ProcessModel pm = pds.getProcessModel(pmId);

			  // Set Security Settings
			  if (updateSecurity) {
				  Object[][] securityArray = {adminUsersAndGroups, editorUsersAndGroups, managerUsersAndGroups, viewerUsersAndGroups, initiatorUsersAndGroups, denyUsersAndGroups};
				  String[] securityRoles = {"ADMIN_OWNER", "EDITOR", "MANAGER", "VIEWER", "INITIATOR", "EXPLICIT_NONMEMBER"};
				  RoleMap rm = new RoleMap();

				  for (int i = 0; i < securityArray.length; i++) {

					  List<Long> groupsList = new ArrayList<Long>();
					  List<String> usersList = new ArrayList<String>();
					  for (LocalObject userOrGroup: (LocalObject[]) securityArray[i]) {
						  if (userOrGroup.getType() == 4096) {
							  groupsList.add(userOrGroup.getId());
						  } else if (userOrGroup.getType() == 4097) {
							  usersList.add(userOrGroup.getStringId());
						  }
					  }
					  if (groupsList.size() > 0) {
						  Long[] groups = new Long[groupsList.size()];
						  groups = groupsList.toArray(groups);
						  rm.addActorsToRole(securityRoles[i], RoleMap.TYPE_GROUP, groups);
					  }
					  if (usersList.size() > 0) {
						  String[] users = new String[usersList.size()];
						  users = usersList.toArray(users);
						  rm.addActorsToRole(securityRoles[i], RoleMap.TYPE_USER, users);
					  }
				  }

				  try {
					  pds.setSecurityForProcessModel(pm.getId(), new Security(rm));
				  } catch (Exception e) {
					  throw createException(e, "error.setSecurity", e.getMessage());
				  }
			  }

			  // Set Cleanup Settings
			  if (updateCleanup) {
				  
				  try {
					  pm.setCleanupAction(cleanupType);
					  if (cleanupType == AbstractProcessModel.AUTO_ARCHIVE) {
						  pm.setAutoArchiveDelay(cleanupDelay);
					  } else if (cleanupType == AbstractProcessModel.AUTO_DELETE) {
						  pm.setAutoDeleteDelay(cleanupDelay);
					  } else if (cleanupType == AbstractProcessModel.NO_CLEANUP) {
						  //pm.setAutoArchiveDelay(0);
					  } else if (cleanupType == AbstractProcessModel.SYSTEM_DEFAULT) {
						  pm.setAutoArchiveDelay(defaultArchivalDays);
					  }
				  } catch (Exception e) {
					  throw createException(e, "error.setCleanup", e.getMessage());
				  }
			  }

			  // Set Alert Settings
			  if (updateAlerts) {
				  try {
					  ProcessModelNotificationSettings ntfs = pm.getNtfSettings();
					  ntfs.setCustomSettings(isCustomSetting); 
					  ntfs.setNotifyInitiator(isNotifyInitiator);
					  ntfs.setNotifyOwner(isNotifyOwner);
					  ntfs.setNotifyRecipientsInExpression(isNotifyRecipientsInExpression);
					  ntfs.setRecipientsExpression(notifyRecipientsInExpression);
					  ntfs.setNotifyUsersAndGroups(isNotifyUsersAndGroups);
					  ntfs.setUsersAndGroups((LocalObject[]) notifyUsersAndGroups);
				  } catch (Exception e) {
					  throw createException(e, "error.setAlerts", e.getMessage());
				  }
			  }
			  try {
				  pds.updateProcessModel(pm);
			  } catch (Exception e) {
				  throw createException(e, "error.updateProcessModel", e.getMessage());
			  }
		  }

	  } catch (InvalidProcessModelException ipe) {
		  	errorMessage = "error.invalidProcessModel";
	        throw createException(ipe, errorMessage, ipe.getMessage());
	  }	catch (Exception e) {
	        errorMessage = "error.generic";
	        throw createException(e, errorMessage, e.getMessage());
	  }
  }

  public SetProcessModelsSettings(SmartServiceContext smartServiceCtx, ProcessDesignService pds, GlobalizationService gls, GroupService gs) {
    super();
    this.smartServiceCtx = smartServiceCtx;
    this.gls = gls;
    this.pds = pds;
    this.gs = gs;
  }

  @Override
  public void onSave(MessageContainer messages) {

  }

  @Override
  public void validate(MessageContainer messages) {
	  if (updateSecurity) {
		  if (adminUsersAndGroups.length == 0 &&
				  editorUsersAndGroups.length == 0 &&
				  managerUsersAndGroups.length == 0 &&
				  viewerUsersAndGroups.length == 0 &&
				  initiatorUsersAndGroups.length == 0 &&
				  denyUsersAndGroups.length == 0) {
			  messages.addError("UpdateSecurity", "validation.securityNull");
		  }
	  }

	  if (updateCleanup) {
		  if (cleanupType == null) {
			  messages.addError("CleanupType", "validation.cleanupTypeNull");
		  }
		  if (cleanupType != AbstractProcessModel.NO_CLEANUP && cleanupDelay == null) {
			  messages.addError("CleanupDelay", "validation.cleanupDelayNull");
		  }
	  }

	  if (updateAlerts) {

		  if (isNotifyRecipientsInExpression) {
			  if (notifyRecipientsInExpression == null) {
				  messages.addError("NotifyRecipientsInExpression", "validation.expressionNull");
			  }
		  }

		  if (isNotifyUsersAndGroups) {
			  if (notifyUsersAndGroups.length == 0) {
				  messages.addError("NotifyUsersAndGroups", "validation.usersAndGroupsNull");
			  }
		  }
	  }
  }

  @Input(required = Required.ALWAYS)
  @Name("ProcessModelIds")
  public void setIds(Long[] processModelIds) {
    this.processModelIds = processModelIds;
  }

  @Input(required = Required.ALWAYS)
  @Name("UpdateSecurity")
  public void setUpdateSecurity(Boolean updateSecurity) {
	  this.updateSecurity = updateSecurity;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("SecurityAdmin")
  public void setAdminUsersAndGroups(Object[] adminUsersAndGroups) {
	  this.adminUsersAndGroups = adminUsersAndGroups;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("SecurityEditor")
  public void setEditorUsersAndGroups(Object[] editorUsersAndGroups) {
	  this.editorUsersAndGroups = editorUsersAndGroups;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("SecurityManager")
  public void setManagerUsersAndGroups(Object[] managerUsersAndGroups) {
	  this.managerUsersAndGroups = managerUsersAndGroups;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("SecurityViewer")
  public void setViewerUsersAndGroups(Object[] viewerUsersAndGroups) {
	  this.viewerUsersAndGroups = viewerUsersAndGroups;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("SecurityInitiator")
  public void setInitiatorUsersAndGroups(Object[] initiatorUsersAndGroups) {
	  this.initiatorUsersAndGroups = initiatorUsersAndGroups;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("SecurityDeny")
  public void setDenyUsersAndGroups(Object[] denyUsersAndGroups) {
	  this.denyUsersAndGroups = denyUsersAndGroups;
  }

  @Input(required = Required.ALWAYS)
  @Name("UpdateCleanup")
  public void setUpdateCleanup(Boolean updateCleanup) {
	  this.updateCleanup = updateCleanup;
  }

  @Input(required = Required.OPTIONAL,  enumeration="CLEANUPTYPE", defaultValue = "3")
  @Name("CleanupType")
  public void setCleanupType(Integer cleanupType) {
	  this.cleanupType = cleanupType;
  }

  @Input(required = Required.OPTIONAL)
  @Name("CleanupDelay")
  public void setCleanupDelay(Integer cleanupDelay) {
	  this.cleanupDelay = cleanupDelay;
  }

  @Input(required = Required.ALWAYS)
  @Name("UpdateAlerts")
  public void setUpdateAlerts(Boolean updateAlerts) {
	  this.updateAlerts = updateAlerts;
  }

  @Input(required = Required.OPTIONAL)
  @Name("IsCustomSetting")
  public void setIsCustomSetting(Boolean isCustomSetting) {
	  this.isCustomSetting = isCustomSetting;
  }

  @Input(required = Required.OPTIONAL)
  @Name("IsNotifyInitiator")
  public void setIsNotifyInitiator(Boolean isNotifyInitiator) {
	  this.isNotifyInitiator = isNotifyInitiator;
  }

  @Input(required = Required.OPTIONAL)
  @Name("IsNotifyOwner")
  public void setIsNotifyOwner(Boolean isNotifyOwner) {
	  this.isNotifyOwner = isNotifyOwner;
  }

  @Input(required = Required.OPTIONAL)
  @Name("IsNotifyRecipientsInExpression")
  public void setIsNotifyRecipientsInExpression(Boolean isNotifyRecipientsInExpression) {
	  this.isNotifyRecipientsInExpression = isNotifyRecipientsInExpression;
  }

  @Input(required = Required.OPTIONAL)
  @Name("NotifyRecipientsInExpression")
  public void setNotifyRecipientsInExpression(String notifyRecipientsInExpression) {
	  this.notifyRecipientsInExpression = notifyRecipientsInExpression;
  }

  @Input(required = Required.OPTIONAL)
  @Name("IsNotifyUsersAndGroups")
  public void setIsNotifyUsersAndGroups(Boolean isNotifyUsersAndGroups) {
	  this.isNotifyUsersAndGroups = isNotifyUsersAndGroups;
  }

  @Input(required = Required.OPTIONAL)
  @UserOrGroupDataType
  @Name("NotifyUsersAndGroups")
  public void setNotifyUsersAndGroups(Object[] notifyUsersAndGroups) {
	  this.notifyUsersAndGroups = notifyUsersAndGroups;
  }

  private SmartServiceException createException(Throwable t, String key, Object... args) {
    return new SmartServiceException.Builder(getClass(), t).userMessage(key, args).build();
  }
}
