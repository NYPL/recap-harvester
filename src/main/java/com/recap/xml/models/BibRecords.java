package com.recap.xml.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bibRecords")
@XmlAccessorType(XmlAccessType.FIELD)
public class BibRecords {

  private List<BibRecord> bibRecord;

  @XmlElement(name = "bibRecord")
  public List<BibRecord> getBibRecords() {
    return bibRecord;
  }

  public void setBibRecords(List<BibRecord> bibRecords) {
    this.bibRecord = bibRecords;
  }
}
