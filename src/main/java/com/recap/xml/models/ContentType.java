package com.recap.xml.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "content", propOrder = {"collection"})

@XmlRootElement(name = "content")
public class ContentType {
  @XmlElement(required = true, nillable = true, namespace = "http://www.loc.gov/MARC21/slim")
  protected CollectionType collection;

  public CollectionType getCollection() {
    return collection;
  }

  public void setCollection(CollectionType collection) {
    this.collection = collection;
  }
}
