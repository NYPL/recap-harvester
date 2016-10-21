package com.recap.xml.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bib {
    @XmlElement
    private String owningInstitutionId;
    @XmlElement
    private String owningInstitutionBibId;
    @XmlElement(required = true, nillable = true)
    protected ContentType content;

    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    public String getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(String owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    public ContentType getContent() {
        return content;
    }

    public void setContent(ContentType content) {
        this.content = content;
    }

}
