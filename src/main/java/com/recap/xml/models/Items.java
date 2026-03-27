package com.recap.xml.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "items")
@XmlAccessorType(XmlAccessType.FIELD)
public class Items {

  @XmlElement(required = true, nillable = true)
  protected ContentType content;

  public ContentType getContent() {
    return content;
  }

  public void setContent(ContentType content) {
    this.content = content;
  }
}
