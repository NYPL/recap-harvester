package com.recap.xml.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Holdings {
    @XmlElement
    private List<Holding> holding;

  public List<Holding> getHolding() {
    return holding;
  }

  public void setHolding(List<Holding> holding) {
    this.holding = holding;
  }
}
