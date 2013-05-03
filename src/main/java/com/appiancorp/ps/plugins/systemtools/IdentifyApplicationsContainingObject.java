/**
 * The "Identify Applications Containing Object" plugin smart service will scan all
 *  Appian Applications and identify which applications contain a specified Appian object.
 *
 *  Inputs:
 *   Object Id - The ID of the object to scan for. If not provided, then Object Uuid will be used.
 *   Object Uuid - The UUID of the object to scan for. This may be used instead of Object Id.
 *   Object Type - The Appian type of the object to scan for.
 *   Include Draft Applications - If true, this will scan all applications including draft applications.
 *    If false, this will only scan published applications.
 *
 *  Outputs:
 *   Application Names - A list of the names of all applications containing the object.
 *   Application URLs - A list of the urls of all applications containing the object.
 *
 * @author jason.ruvinsky@appian.com
 */

package com.appiancorp.ps.plugins.systemtools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.appiancorp.ix.Type;
import com.appiancorp.ix.TypeIxTypeResolver;
import com.appiancorp.ix.binding.ExportBinderMap;
import com.appiancorp.services.ServiceContext;
import com.appiancorp.services.ServiceContextFactory;
import com.appiancorp.suiteapi.applications.Application;
import com.appiancorp.suiteapi.applications.ApplicationService;
import com.appiancorp.suiteapi.common.Constants;
import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.common.ResultPage;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Order;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.framework.Unattended;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;

@PaletteInfo(paletteCategory="Appian Smart Services", palette="System Tools")
@Unattended
@Order({"ObjectId", "ObjectUuid", "ObjectType"})
public class IdentifyApplicationsContainingObject extends AppianSmartService {

  private static final Logger LOG = Logger.getLogger(IdentifyApplicationsContainingObject.class);

  // services
  private final SmartServiceContext smartServiceCtx;
  private final ApplicationService as;
  private final ContentService cs;

  // inputs
  private String objectUuid;
  private Long objectId;
  private Long objectType;
  private boolean includeDrafts;

  // outputs
  private String[] containingApplicationNames;
  private String[] containingApplicationUrls;


  /**
   * @throws SmartServiceException
   */
  @Override
  public void run() throws SmartServiceException {

    if (objectId != null) {

      // Get ID from UUID
      ServiceContext sc = ServiceContextFactory.getServiceContext(smartServiceCtx.getUsername());
      ExportBinderMap ebm = new ExportBinderMap(sc);

      try {
        objectUuid = (String) TypeHelper.getType(ebm, convertObjectTypeToIxType().getKey(), objectId);
      } catch (Exception e) {
        throw generateSmartServiceException("Appian object not found. Type: " + convertObjectTypeToIxType().getKey() + " with id: " +
          objectId, true);
      }
    }

    if (objectUuid != null && !(objectUuid.equals(""))) {

      // Get all applications (Note: if there are a large number of applications on the system, this may
      // adversely affect performance)
      ResultPage applicationsPaging = as.getApplicationsPaging(0, Constants.COUNT_ALL,
        ContentConstants.COLUMN_NAME, Constants.SORT_ORDER_ASCENDING, includeDrafts);

      if (applicationsPaging.getAvailableItems() == 0) {
        if (includeDrafts) {
          throw generateSmartServiceException(
            "No Applications found. Please run as a user that has access to all the applications to be scanned for the object.",
            true);
        } else {
          throw generateSmartServiceException(
            "No Published Applications found. Please run as a user that has access to all the published applications to be scanned"
              + "for the object, or include draft applications.",
              true);
        }
      }

      Application[] allApplications = (Application[])applicationsPaging.getResults();
      List<Application> allApplicationsList = new ArrayList<Application>(Arrays.asList(allApplications));

      // Scan applications for object and generate lists of app names and urls containing the object
      List<String> applicationNames = new ArrayList<String>(allApplicationsList.size());
      List<String> applicationUrls = new ArrayList<String>(allApplicationsList.size());

      for (Application application : allApplicationsList) {
        if (isObjectInApplication(application)) {
          applicationNames.add(application.getName());
          applicationUrls.add(application.getUrlIdentifier());
        }
      }

      containingApplicationNames = applicationNames.toArray(new String[applicationNames.size()]);
      containingApplicationUrls = applicationUrls.toArray(new String[applicationUrls.size()]);
    } else {
      throw generateSmartServiceException("No object ID or UUID provided. Cannot identify applications containing an object without either an ID or UUID to scan for.", false);
    }
  }

  public IdentifyApplicationsContainingObject(SmartServiceContext smartServiceCtx, ApplicationService as,
    ContentService cs) {
    super();
    this.smartServiceCtx = smartServiceCtx;
    this.as = as;
    this.cs = cs;
  }

  @Override
  public void onSave(MessageContainer messages) {
  }

  @Override
  public void validate(MessageContainer messages) {
  }

  // *********************************************
  // HELPER FUNCTIONS
  // *********************************************

  private boolean isObjectInApplication(Application app) {

    Type<?,?,?> ixType = convertObjectTypeToIxType();

    // Scan application for object uuid
    Set<?> objectsByType = app.getObjectsByType(ixType);
    return objectsByType.contains(objectUuid);
  }

  private SmartServiceException generateSmartServiceException(String message, boolean includeUserCtx) {
    if (includeUserCtx) {
      message += " [User Context: " + smartServiceCtx.getUsername() + "]";
    }

    SmartServiceException.Builder sseBuilder = new SmartServiceException.Builder(
      IdentifyApplicationsContainingObject.class, new Throwable(message));
    return sseBuilder.build();
  }

  private Type<?,?,?> convertObjectTypeToIxType() {
    return TypeIxTypeResolver.getIxType(objectType);
  }

  // *********************************************
  // INPUT SETTERS
  // *********************************************
  @Input(required = Required.OPTIONAL)
  @Name("ObjectUuid")
  public void setObjectUuid(String objectUuid) {
    this.objectUuid = objectUuid;
  }

  @Input(required = Required.OPTIONAL)
  @Name("ObjectId")
  public void setObjectId(Long objectId) {
    this.objectId = objectId;
  }


  @Input(required = Required.ALWAYS, enumeration = "appian-type")
  @Name("ObjectType")
  public void setObjectType(Long objectType) {
    this.objectType = objectType;
  }

  @Input(required = Required.ALWAYS)
  @Name("IncludeDraftApplications")
  public void setIncludeDrafts(boolean includeDrafts) {
    this.includeDrafts = includeDrafts;
  }

  // *********************************************
  // OUTPUT GETTERS
  // *********************************************
  public String[] getContainingApplicationNames() {
    return containingApplicationNames;
  }

  public String[] getContainingApplicationUrls() {
    return containingApplicationUrls;
  }

}
