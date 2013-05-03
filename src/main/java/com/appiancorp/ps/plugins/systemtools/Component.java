package com.appiancorp.ps.plugins.systemtools;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(namespace=Component.NAMESPACE, name=Component.LOCAL_PART)
@XmlType(namespace=Component.NAMESPACE, name=Component.LOCAL_PART, propOrder={"id", "name", "type", "uuid", "username", "company"})
public class Component implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String NAMESPACE = "urn:appian:ps:plugins:systemtools";
  public static final String LOCAL_PART = "SYSTEMTOOLS_Component";
  public static final QName QNAME = new QName(NAMESPACE, LOCAL_PART);

  private Long id;
  private String name;
  private String type;
  private String uuid;
  private String username;
  private String company;

  public Component(Long id, String name, String type, String uuid, String username, String company) {
    this();
    setId(id);
	setName(name);
	setType(type);
	setUuid(uuid);
	setUsername(username);
	setCompany(company);
  }

  public Component(String name, String type, String uuid) {
	this();
	setName(name);
	setType(type);
	setUuid(uuid);
  }

  public Component() {} // for serialization only

  @Id
  @GeneratedValue
  @XmlElement
  public Long getId() {
	return id;
  }
  private void setId(Long id) {
	this.id = id;
  }

  @XmlElement
  public String getName() {
    return name;
  }
  private void setName(String name) {
    this.name = name;
  }

  @XmlElement
  public String getType() {
    return type;
  }
  private void setType(String type) {
    this.type = type;
  }

  @XmlElement
  public String getUuid() {
    return uuid;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @XmlElement
  public String getUsername() {
    return username;
  }
  private void setUsername(String username) {
    this.username = username;
  }

  @XmlElement
  public String getCompany() {
    return company;
  }
  private void setCompany(String company) {
    this.company = company;
  }
}
