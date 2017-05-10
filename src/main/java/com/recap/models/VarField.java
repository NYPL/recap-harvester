package com.recap.models;

import java.util.List;

public class VarField {

  private String fieldTag;
  private String marcTag;
  private String ind1;
  private String ind2;
  private List<SubField> subFields;
  private String content;

  public String getFieldTag() {
    return fieldTag;
  }

  public void setFieldTag(String fieldTag) {
    this.fieldTag = fieldTag;
  }

  public String getMarcTag() {
    return marcTag;
  }

  public void setMarcTag(String marcTag) {
    this.marcTag = marcTag;
  }

  public String getInd1() {
    return ind1;
  }

  public void setInd1(String ind1) {
    this.ind1 = ind1;
  }

  public String getInd2() {
    return ind2;
  }

  public void setInd2(String ind2) {
    this.ind2 = ind2;
  }

  public List<SubField> getSubFields() {
    return subFields;
  }

  public void setSubFields(List<SubField> subFields) {
    this.subFields = subFields;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
