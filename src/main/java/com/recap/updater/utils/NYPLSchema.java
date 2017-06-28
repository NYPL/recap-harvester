package com.recap.updater.utils;

import org.springframework.stereotype.Component;

@Component
public class NYPLSchema {

  private String bibSchemaJson;

  private String itemSchemaJson;

  public String getBibSchemaJson() {
    return bibSchemaJson;
  }

  public void setBibSchemaJson(String bibSchemaJson) {
    this.bibSchemaJson = bibSchemaJson;
  }

  public String getItemSchemaJson() {
    return itemSchemaJson;
  }

  public void setItemSchemaJson(String itemSchemaJson) {
    this.itemSchemaJson = itemSchemaJson;
  }

}
