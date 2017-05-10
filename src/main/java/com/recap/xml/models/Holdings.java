package com.recap.xml.models;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;


public class Holdings {
  private List<Holding> holding;

  @XmlElement
  public List<Holding> getHolding() {
    return holding;
  }

  public void setHolding(List<Holding> holding) {
    this.holding = holding;
  }
}
