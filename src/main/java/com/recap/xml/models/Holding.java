package com.recap.xml.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Holding {

    @XmlElement
    private String owningInstitutionHoldingsId;

    @XmlElement(required = true, nillable = true)
    protected ContentType content;

    @XmlElement
    private List<Items> items;

    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
    }

    public ContentType getContent() {
        return content;
    }

    public void setContent(ContentType content) {
        this.content = content;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }
}