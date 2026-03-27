package com.recap.xml.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "bibRecords")
@XmlAccessorType(XmlAccessType.FIELD)
public class BibRecords {

  @XmlElement(name = "bibRecord")
  private List<BibRecord> bibRecord;

  public List<BibRecord> getBibRecords() {
    return bibRecord;
  }

  public void setBibRecords(List<BibRecord> bibRecords) {
    this.bibRecord = bibRecords;
  }
}
