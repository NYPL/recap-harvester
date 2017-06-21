package com.recap.models;

import java.util.List;
import java.util.Map;

public class Item {

  private String id;
  private String nyplSource;
  private String nyplType;
  private List<String> bibIds;
  private String updatedDate;
  private String createdDate;
  private boolean deleted;
  private Map<String, String> location;
  private Map<String, String> status;
  private String barcode;
  private String callNumber;
  private Map<String, Map<String, Object>> fixedFields;
  private List<VarField> varFields;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNyplSource() {
    return nyplSource;
  }

  public void setNyplSource(String nyplSource) {
    this.nyplSource = nyplSource.toLowerCase();
  }

  public String getNyplType() {
    return nyplType;
  }

  public void setNyplType(String nyplType) {
    this.nyplType = nyplType.toLowerCase();
  }

  public List<String> getBibIds() {
    return bibIds;
  }

  public void setBibIds(List<String> bibIds) {
    this.bibIds = bibIds;
  }

  public String getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(String updatedDate) {
    this.updatedDate = updatedDate;
  }

  public String getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(String createdDate) {
    this.createdDate = createdDate;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Map<String, String> getLocation() {
    return location;
  }

  public void setLocation(Map<String, String> location) {
    this.location = location;
  }

  public Map<String, String> getStatus() {
    return status;
  }

  public void setStatus(Map<String, String> status) {
    this.status = status;
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getCallNumber() {
    return callNumber;
  }

  public void setCallNumber(String callNumber) {
    this.callNumber = callNumber;
  }

  public Map<String, Map<String, Object>> getFixedFields() {
    return fixedFields;
  }

  public void setFixedFields(Map<String, Map<String, Object>> fixedFields) {
    this.fixedFields = fixedFields;
  }

  public List<VarField> getVarFields() {
    return varFields;
  }

  public void setVarFields(List<VarField> varFields) {
    this.varFields = varFields;
  }
}
