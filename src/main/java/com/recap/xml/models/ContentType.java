package com.recap.xml.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "content", propOrder = {
        "collection"
})

@XmlRootElement(name = "content")
public class ContentType {
    @XmlElement(required = true, nillable = true)
    protected CollectionType collection;

    public CollectionType getCollection() {
        return collection;
    }

    public void setCollection(CollectionType collection) {
        this.collection = collection;
    }
}