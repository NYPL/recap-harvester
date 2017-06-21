package com.recap.models;

import java.util.List;
import java.util.Map;

public class Bib {

  private String id;
  private String nyplSource;
  private String nyplType;
  private String updatedDate;
  private String createdDate;
  private Boolean deleted;
  private Boolean suppressed;
  private Map<String, String> lang;
  private String title;
  private String author;
  private Map<String, String> materialType;
  private Map<String, String> bibLevel;
  private Integer publishYear;
  private Map<String, String> country;
  private Map<String, Map<String, String>> fixedFields;
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

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public Boolean getSuppressed() {
    return suppressed;
  }

  public void setSuppressed(Boolean suppressed) {
    this.suppressed = suppressed;
  }

  public Map<String, String> getLang() {
    return lang;
  }

  public void setLang(Map<String, String> lang) {
    this.lang = lang;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Map<String, String> getMaterialType() {
    return materialType;
  }

  public void setMaterialType(Map<String, String> materialType) {
    this.materialType = materialType;
  }

  public Map<String, String> getBibLevel() {
    return bibLevel;
  }

  public void setBibLevel(Map<String, String> bibLevel) {
    this.bibLevel = bibLevel;
  }

  public Integer getPublishYear() {
    return publishYear;
  }

  public void setPublishYear(Integer publishYear) {
    this.publishYear = publishYear;
  }

  public Map<String, String> getCountry() {
    return country;
  }

  public void setCountry(Map<String, String> country) {
    this.country = country;
  }

  public Map<String, Map<String, String>> getFixedFields() {
    return fixedFields;
  }

  public void setFixedFields(Map<String, Map<String, String>> fixedFields) {
    this.fixedFields = fixedFields;
  }

  public List<VarField> getVarFields() {
    return varFields;
  }

  public void setVarFields(List<VarField> varFields) {
    this.varFields = varFields;
  }

}
