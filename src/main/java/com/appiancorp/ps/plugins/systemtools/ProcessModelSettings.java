package com.appiancorp.ps.plugins.systemtools;

import java.io.Serializable;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(namespace="http://coe.appiancorp.com/suite/types/", name="component")
@XmlType(namespace="http://coe.appiancorp.com/suite/types/", name=ProcessModelSettings.LOCAL_PART, propOrder={"id", "uuid", "name", "folderStructure", "administratorUsersAndGroups", "editorUsersAndGroups", "managerUsersAndGroups", "viewerUsersAndGroups", "initiatorUsersAndGroups", "denyUsersAndGroups", "cleanupType", "cleanupDelay", "alerts"})
public class ProcessModelSettings implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String NAMESPACE = "urn:appian:ps:plugins:systemtools";
  public static final String LOCAL_PART = "SYSTEMTOOLS_ProcessModelSettings";
  public static final QName QNAME = new QName(NAMESPACE, LOCAL_PART);

  private Long id;
  private String uuid;
  private String name;
  private String folderStructure;
  private String[] administratorUsersAndGroups;
  private String[] editorUsersAndGroups;
  private String[] managerUsersAndGroups;
  private String[] viewerUsersAndGroups;
  private String[] initiatorUsersAndGroups;
  private String[] denyUsersAndGroups;
  private String cleanupType;
  private Integer cleanupDelay;
  private String[] alerts;


  public ProcessModelSettings(Long id, String uuid, String name, String folderStructure, String[] administratorUsersAndGroups, String[] editorUsersAndGroups, String[] managerUsersAndGroups, String[] viewerUsersAndGroups, String[] initiatorUsersAndGroups, String[] denyUsersAndGroups, String cleanupType, Integer CleanupDelay, String[] alerts) {
    this();
    setId(id);
	setUuid(uuid);
	setName(name);
	setFolderStructure(folderStructure);
    setAdministratorUsersAndGroups(administratorUsersAndGroups);
    setEditorUsersAndGroups(editorUsersAndGroups);
    setManagerUsersAndGroups(managerUsersAndGroups);
    setViewerUsersAndGroups(viewerUsersAndGroups);
    setInitiatorUsersAndGroups(initiatorUsersAndGroups);
    setDenyUsersAndGroups(denyUsersAndGroups);
    setCleanupType(cleanupType);
    setCleanupDelay(CleanupDelay);
    setAlerts(alerts);
  }
 
  public ProcessModelSettings(String uuid, String name, String folderStructure, String[] administratorUsersAndGroups, String[] editorUsersAndGroups, String[] managerUsersAndGroups, String[] viewerUsersAndGroups, String[] initiatorUsersAndGroups, String[] denyUsersAndGroups, String cleanupType, Integer CleanupDelay, String[] alerts) {
	    this();
		setUuid(uuid);
		setName(name);
		setFolderStructure(folderStructure);
	    setAdministratorUsersAndGroups(administratorUsersAndGroups);
	    setEditorUsersAndGroups(editorUsersAndGroups);
	    setManagerUsersAndGroups(managerUsersAndGroups);
	    setViewerUsersAndGroups(viewerUsersAndGroups);
	    setInitiatorUsersAndGroups(initiatorUsersAndGroups);
	    setDenyUsersAndGroups(denyUsersAndGroups);
	    setCleanupType(cleanupType);
	    setCleanupDelay(CleanupDelay);
	    setAlerts(alerts);
	  }

  public ProcessModelSettings() {} // for serialization only

  @Id
  @XmlElement
  public Long getId() {
	return id;
  }
  private void setId(Long id) {
	this.id = id;
  }

  @XmlElement
  public String getUuid() {
    return uuid;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @XmlElement
  public String getName() {
    return name;
  }
  private void setName(String name) {
    this.name = name;
  }

  @XmlElement
  public String getFolderStructure() { 
    return folderStructure;
  }
  private void setFolderStructure(String folderStructure) {
    this.folderStructure = folderStructure;
  }
  
  @XmlElement
  public String[] getAdministratorUsersAndGroups() {
	  return administratorUsersAndGroups;
  }

  private void setAdministratorUsersAndGroups(String[] administratorUsersAndGroups) {
	  this.administratorUsersAndGroups = administratorUsersAndGroups;
  }

  @XmlElement
  public String[] getEditorUsersAndGroups() {
	  return editorUsersAndGroups;
  }

  private void setEditorUsersAndGroups(String[] editorUsersAndGroups) {
	  this.editorUsersAndGroups = editorUsersAndGroups;
  }

  @XmlElement
  public String[] getManagerUsersAndGroups() {
	  return managerUsersAndGroups;
  }

  private void setManagerUsersAndGroups(String[] managerUsersAndGroups) {
	  this.managerUsersAndGroups = managerUsersAndGroups;
  }

  @XmlElement
  public String[] getViewerUsersAndGroups() {
	  return viewerUsersAndGroups;
  }

  private void setViewerUsersAndGroups(String[] viewerUsersAndGroups) {
	  this.viewerUsersAndGroups = viewerUsersAndGroups;
  }

  @XmlElement
  public String[] getInitiatorUsersAndGroups() {
	  return initiatorUsersAndGroups;
  }

  private void setInitiatorUsersAndGroups(String[] initiatorUsersAndGroups) {
	  this.initiatorUsersAndGroups = initiatorUsersAndGroups;
  }

  @XmlElement
  public String[] getDenyUsersAndGroups() {
	  return denyUsersAndGroups;
  }

  private void setDenyUsersAndGroups(String[] denyUsersAndGroups) {
	  this.denyUsersAndGroups = denyUsersAndGroups;
  }

  @XmlElement
  public String getCleanupType() {
	  return cleanupType;
  }

  private void setCleanupType(String cleanupType) {
	  this.cleanupType = cleanupType;
  }

  @XmlElement
  public Integer getCleanupDelay() {
	  return cleanupDelay;
  }

  private void setCleanupDelay(Integer cleanupDelay) {
	  this.cleanupDelay = cleanupDelay;
  }

  @XmlElement
  public String[] getAlerts() {
	  return alerts;
  }

  private void setAlerts(String[] alerts) {
	  this.alerts = alerts;
  }
}
