package com.recap.updater.bib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.recap.config.BaseConfig;
import com.recap.exceptions.BibFieldProcessingException;
import com.recap.models.Bib;
import com.recap.models.Subfield;
import com.recap.models.VarField;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.ControlFieldType;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.RecordType;
import com.recap.xml.models.SubfieldatafieldType;

public class BibFieldsProcessor {

  private BaseConfig baseConfig;

  private static Logger logger = LoggerFactory.getLogger(BibFieldsProcessor.class);

  public BibFieldsProcessor(BaseConfig baseConfig) {
    this.baseConfig = baseConfig;
  }

  public Map<String, String> getLanguageField(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    try {
      Map<String, String> langCodeName = new LinkedHashMap<>();
      String langCode = getLanguageCode(bibRecordType, bib);
      if (langCode == null)
        return null;
      try {
        langCodeName.put(BibConstants.CODE, langCode);
        langCodeName.put(BibConstants.NAME, baseConfig.locationCodeVals().get(langCode));
      } catch (NullPointerException nullPointerException) {
        logger.error("FATAL-low : Unable to get name for location code - " + langCode + " for "
            + bib.getNyplType() + " - " + bib.getId() + ", " + "source - " + bib.getNyplSource());
        langCodeName.put(BibConstants.CODE, langCode);
        langCodeName.put(BibConstants.NAME, null);
      }
      return langCodeName;
    } catch (JsonParseException jsonException) {
      logger.error("JsonParseException occurred while getting language field - ", jsonException);
      throw new BibFieldProcessingException("JsonParseException occurred during"
          + " language processing - " + jsonException.getMessage());
    } catch (JsonMappingException jsonException) {
      logger.error("JsonMappingException occurred while getting language field - ", jsonException);
      throw new BibFieldProcessingException("JsonMappingException occurred during"
          + " language processing - " + jsonException.getMessage());
    } catch (IOException ioe) {
      logger.error("IOException occurred while getting language field - ", ioe);
      throw new BibFieldProcessingException(
          "IOException occurred during" + " language processing - " + ioe.getMessage());
    }
  }

  public String getLanguageCode(List<RecordType> bibRecordType, Bib bib)
      throws JsonParseException, JsonMappingException, IOException {
    List<ControlFieldType> controlFields =
        bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getControlfield();
    StringBuffer lang = new StringBuffer();
    boolean isProcessed = false;
    for (ControlFieldType controlFieldType : controlFields) {
      if (controlFieldType.getTag().equals(BibConstants.CONTROL_FIELD_TYPE_008) && !isProcessed) {
        String langCode = null;
        try {
          lang.append(controlFieldType.getValue());
          langCode = lang.substring(BibConstants.CONTROL_FIELD_LANG_CODE_START_INDEX,
              BibConstants.CONTROL_FIELD_LANG_CODE_END_INDEX);
          isProcessed = true;
          return langCode.trim();
        } catch (StringIndexOutOfBoundsException stringOutOfBounds) {
          logger.error("FATAL-low : Unable to get langCode from control field - "
              + controlFieldType.getValue() + " for " + bib.getNyplType() + " - " + bib.getId()
              + ", " + "source - " + bib.getNyplSource());
        }
      }
    }
    return null;
  }

  public String getTitle(List<RecordType> bibRecordType) {
    boolean isProcessed = false;
    for (DataFieldType dataFieldType : bibRecordType.get(BibConstants.BIB_RECORD_NUMBER)
        .getDatafield()) {
      if (dataFieldType.getTag().equals(BibConstants.DATAFIELD_TYPE_TITLE_CODE) && !isProcessed) {
        StringBuffer stringBuffer = new StringBuffer();
        for (SubfieldatafieldType subField : dataFieldType.getSubfield()) {
          stringBuffer.append(subField.getValue() + " ");
        }
        isProcessed = true;
        return stringBuffer.toString().trim();
      }
    }
    return null;
  }

  public String getAuthor(List<RecordType> bibRecordType) {
    boolean isProcessed = false;
    for (DataFieldType dataFieldType : bibRecordType.get(BibConstants.BIB_RECORD_NUMBER)
        .getDatafield()) {
      if (dataFieldType.getTag().equals(BibConstants.DATAFIELD_TYPE_AUTHOR_CODE) && !isProcessed) {
        StringBuffer stringBuffer = new StringBuffer();
        for (SubfieldatafieldType subField : dataFieldType.getSubfield()) {
          if (subField.getCode().equals(BibConstants.SUBFIELD_TYPE_AUTHOR_CODE))
            stringBuffer.append(subField.getValue() + " ");
        }
        isProcessed = true;
        return stringBuffer.toString().trim();
      }
    }
    return null;
  }

  public Map<String, String> getMaterialType(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    try {
      Map<String, String> materialTypeInSierraFormat = new LinkedHashMap<>();
      List<ControlFieldType> controlFields =
          bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getControlfield();
      boolean isProcessedControlField = false;
      for (ControlFieldType controlFieldType : controlFields) {
        if (controlFieldType.getTag().equals(BibConstants.CONTROL_FIELD_TYPE_007)
            && !isProcessedControlField
            && controlFieldType.getValue().startsWith(BibConstants.REPR_OF_MICROFORM)) {
          materialTypeInSierraFormat.put(BibConstants.CODE, BibConstants.REPR_OF_MICROFORM);
          materialTypeInSierraFormat.put(BibConstants.VALUE, BibConstants.MICROFORM);
          isProcessedControlField = true;
          return materialTypeInSierraFormat;
        }
      }

      List<DataFieldType> dataFields =
          bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getDatafield();
      boolean isProcessedDataField = false;
      for (DataFieldType dataFieldType : dataFields) {
        if (dataFieldType.getTag().equals(BibConstants.DATAFIELD_TYPE_337)
            && !isProcessedDataField) {
          List<SubfieldatafieldType> subFields = dataFieldType.getSubfield();
          for (SubfieldatafieldType subField : subFields) {
            if (subField.getCode().equals(BibConstants.SUBFIELD_TYPE_b) && !isProcessedDataField
                && subField.getValue().equals(BibConstants.REPR_OF_MICROFORM)) {
              materialTypeInSierraFormat.put(BibConstants.CODE, BibConstants.REPR_OF_MICROFORM);
              materialTypeInSierraFormat.put(BibConstants.VALUE, BibConstants.MICROFORM);
              isProcessedDataField = true;
              return materialTypeInSierraFormat;
            }
          }
        }
      }
      String leaderValue = bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getLeader().getValue();

      Map<String, String> materialCodeVal =
          baseConfig.materialCodeAndVals().get(Character.toString(leaderValue.charAt(6)));
      materialTypeInSierraFormat.put(BibConstants.CODE, Character.toString(leaderValue.charAt(6)));
      try {
        materialTypeInSierraFormat.put(BibConstants.VALUE,
            materialCodeVal.get(BibConstants.SIERRA_LABEL));
      } catch (NullPointerException nullPointerException) {
        logger.error("FATAL-low : Unable to get materialType " + " for " + bib.getNyplType() + " - "
            + bib.getId() + ", " + "source - " + bib.getNyplSource() + ", leader - " + leaderValue);
        materialTypeInSierraFormat.put(BibConstants.CODE, BibConstants.REPR_OF_BOOK_TEXT);
        materialTypeInSierraFormat.put(BibConstants.VALUE, BibConstants.BOOK_TEXT);
      }

      return materialTypeInSierraFormat;
    } catch (JsonParseException jsonException) {
      logger.error("JsonParseException occurred while getting " + "material type field - ",
          jsonException);
      throw new BibFieldProcessingException("JsonParseException occurred during"
          + " material type processing - " + jsonException.getMessage());
    } catch (JsonMappingException jsonException) {
      logger.error("JsonMappingException occurred while getting " + "material type field - ",
          jsonException);
      throw new BibFieldProcessingException("JsonMappingException occurred during"
          + " material type processing - " + jsonException.getMessage());
    } catch (IOException ioe) {
      logger.error("IOException occurred while getting " + "material type field - ", ioe);
      throw new BibFieldProcessingException(
          "IOException occurred during" + " material type  processing - " + ioe.getMessage());
    }
  }

  public Map<String, String> getBibLevel(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    try {
      String leaderValue = bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getLeader().getValue();
      StringBuffer stringBuffer = new StringBuffer(leaderValue.substring(7));
      Map<String, String> materialCodeVal = baseConfig.bibLevelCodeAndVals()
          .get(Character.toString(stringBuffer.toString().charAt(0)));
      Map<String, String> materialTypeInSierraFormat = new HashMap<>();
      materialTypeInSierraFormat.put(BibConstants.CODE,
          Character.toString(stringBuffer.toString().charAt(0)));
      try {
        materialTypeInSierraFormat.put(BibConstants.VALUE,
            materialCodeVal.get(BibConstants.SIERRA_LABEL));
      } catch (NullPointerException nullPointerException) {
        logger.error("FATAL-low : Unable to get bibLevel " + " for " + bib.getNyplType() + " - "
            + bib.getId() + ", " + "source - " + bib.getNyplSource() + ", leader - " + leaderValue);
        materialTypeInSierraFormat.put(BibConstants.CODE, "-");
        materialTypeInSierraFormat.put(BibConstants.VALUE, "---");
      }

      return materialTypeInSierraFormat;
    } catch (JsonParseException jsonException) {
      logger.error("JsonParseException occurred while getting " + "bib level field - ",
          jsonException);
      throw new BibFieldProcessingException("JsonParseException occurred during"
          + " bib level processing - " + jsonException.getMessage());
    } catch (JsonMappingException jsonException) {
      logger.error("JsonMappingException occurred while getting " + "bib level field - ",
          jsonException);
      throw new BibFieldProcessingException("JsonMappingException occurred during"
          + " bib level processing - " + jsonException.getMessage());
    } catch (IOException ioe) {
      logger.error("IOException occurred while getting " + "bib level field - ", ioe);
      throw new BibFieldProcessingException(
          "IOException occurred during" + " bib level processing - " + ioe.getMessage());
    }
  }

  public Integer getPublishYear(List<RecordType> bibRecordType, Bib bib) {
    List<ControlFieldType> controlFields =
        bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getControlfield();
    StringBuffer publishYear = new StringBuffer();
    boolean isProcessed = false;
    for (ControlFieldType controlFieldType : controlFields) {
      if (controlFieldType.getTag().equals(BibConstants.CONTROL_FIELD_TYPE_008) && !isProcessed) {
        try {
          publishYear.append(controlFieldType.getValue());
          Integer publishYearNumber = Integer
              .parseInt(publishYear.substring(BibConstants.CONTROL_FIELD_PUBLISH_YEAR_START_INDEX,
                  BibConstants.CONTROL_FIELD_PUBLISH_YEAR_END_INDEX).trim());
          isProcessed = true;
          return publishYearNumber;
        } catch (StringIndexOutOfBoundsException stringOutOfBounds) {
          logger.error("FATAL-low - Unable to get publishYear from - " + controlFieldType.getValue()
              + " for " + bib.getNyplType() + " - " + bib.getId() + ", " + "source - "
              + bib.getNyplSource());
        } catch (NumberFormatException numberFormatException) {
          logger.error("FATAL-low - Unable to get publishYear from - " + controlFieldType.getValue()
              + " for " + bib.getNyplType() + " - " + bib.getId() + ", " + "source - "
              + bib.getNyplSource());
        }
      }
    }
    return null;
  }

  public Map<String, String> getCountry(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    List<ControlFieldType> controlFields =
        bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getControlfield();
    StringBuffer countryInfo = new StringBuffer();
    boolean isProcessed = false;
    for (ControlFieldType controlFieldType : controlFields) {
      if (controlFieldType.getTag().equals(BibConstants.CONTROL_FIELD_TYPE_008) && !isProcessed) {
        try {
          countryInfo.append(controlFieldType.getValue());
          String countryCode =
              countryInfo.substring(BibConstants.CONTROL_FIELD_COUNTRY_CODE_START_INDEX,
                  BibConstants.CONTROL_FIELD_COUNTRY_CODE_END_INDEX).trim();
          String countryName = baseConfig.countryCodeAndVals().get(countryCode);
          isProcessed = true;
          Map<String, String> countryCodeName = new HashMap<>();
          countryCodeName.put(BibConstants.CODE, countryCode);
          countryCodeName.put(BibConstants.NAME, countryName);
          if (countryName == null)
            return null;
          return countryCodeName;
        } catch (StringIndexOutOfBoundsException stringOutOfBounds) {
          logger.error("FATAL-low - Unable to get publishYear from - " + controlFieldType.getValue()
              + " for " + bib.getNyplType() + " - " + bib.getId() + ", " + "source - "
              + bib.getNyplSource());
        } catch (NumberFormatException numberFormatException) {
          logger.error("FATAL-low - Unable to get publishYear from - " + controlFieldType.getValue()
              + " for " + bib.getNyplType() + " - " + bib.getId() + ", " + "source - "
              + bib.getNyplSource());
        } catch (JsonParseException jsonException) {
          logger.error("JsonParseException occurred while getting " + "country field - ",
              jsonException);
          throw new BibFieldProcessingException("JsonParseException occurred during"
              + " country processing - " + jsonException.getMessage());
        } catch (JsonMappingException jsonException) {
          logger.error("JsonMappingException occurred while getting " + "country field - ",
              jsonException);
          throw new BibFieldProcessingException("JsonMappingException occurred during"
              + " country processing - " + jsonException.getMessage());
        } catch (IOException ioe) {
          logger.error("IOException occurred while getting " + "country field - ", ioe);
          throw new BibFieldProcessingException(
              "IOException occurred during" + " country processing - " + ioe.getMessage());
        }
      }
    }
    return null;
  }

  public Map<String, Map<String, String>> getFixedFields(BibRecord bibRecord,
      List<RecordType> bibRecordType, String updatedDate, Bib bib)
      throws BibFieldProcessingException {
    Map<String, Map<String, String>> fixedFields = new HashMap<>();
    fixedFields.put(BibConstants.FIXED_FIELDS_24, getFixedFieldsForLanguage(bibRecordType, bib));
    fixedFields.put(BibConstants.FIXED_FIELDS_25, getFixedFieldsCharsSkipForTitle());
    fixedFields.put(BibConstants.FIXED_FIELDS_26, getFixedFieldsLocation(bibRecord));
    fixedFields.put(BibConstants.FIXED_FIELDS_27, getFixedFieldsCopies());
    fixedFields.put(BibConstants.FIXED_FIELDS_28, getFixedFieldsForCatalogDate());
    fixedFields.put(BibConstants.FIXED_FIELDS_29, getFixedFieldsForBibLevel(bibRecordType, bib));
    fixedFields.put(BibConstants.FIXED_FIELDS_30,
        getFixedFieldsForMaterialType(bibRecordType, bib));
    fixedFields.put(BibConstants.FIXED_FIELDS_31, getFixedFieldsForBibCode3());
    fixedFields.put(BibConstants.FIXED_FIELDS_80, getFixedFieldsRecordType());
    fixedFields.put(BibConstants.FIXED_FIELDS_81, getFixedFieldsForRecordNumber(bibRecord));
    fixedFields.put(BibConstants.FIXED_FIELDS_83, getFixedFieldsCreatedDate());
    fixedFields.put(BibConstants.FIXED_FIELDS_84, getFixedFieldsForUpdatedDate(updatedDate));
    fixedFields.put(BibConstants.FIXED_FIELDS_85, getFixedFieldsNumberOfRevisions());
    fixedFields.put(BibConstants.FIXED_FIELDS_86, getFixedFieldsAgency());
    fixedFields.put(BibConstants.FIXED_FIELDS_89, getFixedFieldsForCountry(bibRecordType, bib));
    fixedFields.put(BibConstants.FIXED_FIELDS_98, getFixedFieldsPDate());
    fixedFields.put(BibConstants.FIXED_FIELDS_107, getFixedFieldsMARCType());
    return fixedFields;
  }

  public Map<String, String> getFixedFieldsForLanguage(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    Map<String, String> langCodeAndName = getLanguageField(bibRecordType, bib);
    Map<String, String> languageKeysAndVals = new LinkedHashMap<>();
    try {
      languageKeysAndVals.put(BibConstants.LABEL, BibConstants.LANGUAGE);
      languageKeysAndVals.put(BibConstants.VALUE, langCodeAndName.get(BibConstants.CODE));
      languageKeysAndVals.put(BibConstants.DISPLAY, langCodeAndName.get(BibConstants.NAME));
    } catch (NullPointerException nullPointerException) {
      logger.error("FATAL-low - Unable to get language code from - " + " for " + bib.getNyplType()
          + " - " + bib.getId() + ", " + "source - " + bib.getNyplSource());
      return null;
    }
    return languageKeysAndVals;
  }

  public Map<String, String> getFixedFieldsCharsSkipForTitle() {
    Map<String, String> charsSkip = new LinkedHashMap<>();
    charsSkip.put(BibConstants.LABEL, BibConstants.SKIP);
    charsSkip.put(BibConstants.VALUE, BibConstants.SKIP_VALUE);
    return charsSkip;
  }

  public Map<String, String> getFixedFieldsForBibLevel(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    Map<String, String> bibLevelInfo = getBibLevel(bibRecordType, bib);
    Map<String, String> fixedFieldsBibLevel = new LinkedHashMap<>();
    fixedFieldsBibLevel.put(BibConstants.LABEL, BibConstants.BIB_LEVEL);
    fixedFieldsBibLevel.put(BibConstants.VALUE, bibLevelInfo.get(BibConstants.CODE));
    fixedFieldsBibLevel.put(BibConstants.DISPLAY, bibLevelInfo.get(BibConstants.VALUE));
    return fixedFieldsBibLevel;
  }

  public Map<String, String> getFixedFieldsLocation(BibRecord bibRecord) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(BibConstants.RECAP_INITIALS);
    stringBuffer.append(bibRecord.getBib().getOwningInstitutionId().toLowerCase());
    Map<String, String> locationAndValue = new LinkedHashMap<>();
    locationAndValue.put(BibConstants.LABEL, BibConstants.LOCATION);
    locationAndValue.put(BibConstants.VALUE, stringBuffer.toString());
    return locationAndValue;
  }

  public Map<String, String> getFixedFieldsForMaterialType(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    Map<String, String> materialTypeCodeAndVal = getMaterialType(bibRecordType, bib);
    Map<String, String> materialTypeCodeVal = new LinkedHashMap<>();
    materialTypeCodeVal.put(BibConstants.LABEL, BibConstants.MATERIAL_TYPE);
    materialTypeCodeVal.put(BibConstants.VALUE, materialTypeCodeAndVal.get(BibConstants.CODE));
    materialTypeCodeVal.put(BibConstants.DISPLAY, materialTypeCodeAndVal.get(BibConstants.VALUE));
    return materialTypeCodeVal;
  }

  public Map<String, String> getFixedFieldsRecordType() {
    Map<String, String> recordTypeLabelAndVal = new LinkedHashMap<>();
    recordTypeLabelAndVal.put(BibConstants.LABEL, BibConstants.RECORD_TYPE);
    recordTypeLabelAndVal.put(BibConstants.VALUE, BibConstants.RECORD_TYPE_BIB);
    return recordTypeLabelAndVal;
  }

  public Map<String, String> getFixedFieldsForRecordNumber(BibRecord bibRecord) {
    String bibId = bibRecord.getBib().getOwningInstitutionBibId();
    if (bibId.startsWith(".")) {
      bibId = bibId.substring(2, bibId.length() - 1);
    }
    Map<String, String> recordNumberVal = new LinkedHashMap<>();
    recordNumberVal.put(BibConstants.LABEL, BibConstants.RECORD_NUMBER);
    recordNumberVal.put(BibConstants.VALUE, bibId);
    return recordNumberVal;
  }

  public Map<String, String> getFixedFieldsCreatedDate() {
    Map<String, String> createdDateVals = new LinkedHashMap<>();
    createdDateVals.put(BibConstants.LABEL, BibConstants.CREATED_DATE);
    createdDateVals.put(BibConstants.VALUE, null);
    return createdDateVals;
  }

  public Map<String, String> getFixedFieldsCopies() {
    Map<String, String> copies = new LinkedHashMap<>();
    copies.put(BibConstants.LABEL, BibConstants.COPIES);
    copies.put(BibConstants.VALUE, BibConstants.COPIES_VALUE);
    return copies;
  }

  public Map<String, String> getFixedFieldsForCatalogDate() {
    Map<String, String> catDate = new LinkedHashMap<>();
    catDate.put(BibConstants.LABEL, BibConstants.CATALOG_DATE);
    catDate.put(BibConstants.VALUE, null);
    return catDate;
  }

  public Map<String, String> getFixedFieldsForBibCode3() {
    Map<String, String> bibCode3Vals = new LinkedHashMap<>();
    bibCode3Vals.put(BibConstants.LABEL, BibConstants.BIB_CODE_3);
    bibCode3Vals.put(BibConstants.VALUE, null);
    return bibCode3Vals;
  }

  public Map<String, String> getFixedFieldsForUpdatedDate(String updatedDate) {
    Map<String, String> updatedDateVals = new LinkedHashMap<>();
    updatedDateVals.put(BibConstants.LABEL, BibConstants.UPDATED_DATE);
    updatedDateVals.put(BibConstants.VALUE, updatedDate);
    return updatedDateVals;
  }

  public Map<String, String> getFixedFieldsNumberOfRevisions() {
    Map<String, String> numberOfRevisions = new LinkedHashMap<>();
    numberOfRevisions.put(BibConstants.LABEL, BibConstants.NUMBER_REVISIONS);
    numberOfRevisions.put(BibConstants.VALUE, BibConstants.NUMBER_REVISIONS_VALUE);
    return numberOfRevisions;
  }

  public Map<String, String> getFixedFieldsAgency() {
    Map<String, String> agencyVals = new LinkedHashMap<>();
    agencyVals.put(BibConstants.LABEL, BibConstants.AGENCY);
    agencyVals.put(BibConstants.VALUE, BibConstants.AGENCY_VALUE);
    return agencyVals;
  }

  public Map<String, String> getFixedFieldsForCountry(List<RecordType> bibRecordType, Bib bib)
      throws BibFieldProcessingException {
    Map<String, String> countryVals = new LinkedHashMap<>();
    Map<String, String> countryCodeName = getCountry(bibRecordType, bib);
    if (countryCodeName == null)
      return null;
    countryVals.put(BibConstants.LABEL, BibConstants.COUNTRY);
    countryVals.put(BibConstants.VALUE, countryCodeName.get(BibConstants.CODE));
    countryVals.put(BibConstants.DISPLAY, countryCodeName.get(BibConstants.NAME));
    return countryVals;
  }

  public Map<String, String> getFixedFieldsPDate() {
    Map<String, String> pDateVals = new LinkedHashMap<>();
    pDateVals.put(BibConstants.LABEL, BibConstants.PDATE);
    pDateVals.put(BibConstants.VALUE, null);
    return pDateVals;
  }

  public Map<String, String> getFixedFieldsMARCType() {
    Map<String, String> marcType = new LinkedHashMap<>();
    marcType.put(BibConstants.LABEL, BibConstants.MARC_TYPE);
    marcType.put(BibConstants.VALUE, " ");
    return marcType;
  }

  public List<VarField> getVarFields(List<RecordType> bibRecordType, Bib bib) {
    try {
      List<VarField> varFieldObjects = new ArrayList<>();
      List<DataFieldType> varFields =
          bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getDatafield();
      for (DataFieldType varField : varFields) {
        VarField varFieldObj = new VarField();
        varFieldObj.setFieldTag(varField.getId());
        varFieldObj.setInd1(varField.getInd1());
        varFieldObj.setInd2(varField.getInd2());
        varFieldObj.setMarcTag(varField.getTag());
        List<SubfieldatafieldType> subFields = varField.getSubfield();
        List<Subfield> subFieldObjects = new ArrayList<>();
        for (SubfieldatafieldType subField : subFields) {
          Subfield subFieldObj = new Subfield();
          subFieldObj.setContent(subField.getValue());
          subFieldObj.setTag(subField.getCode());
          subFieldObjects.add(subFieldObj);
        }
        varFieldObj.setSubfields(subFieldObjects);
        varFieldObjects.add(varFieldObj);
      }
      VarField varFieldObjLeader = new VarField();
      varFieldObjLeader.setFieldTag("_");
      varFieldObjLeader
          .setContent(bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getLeader().getValue());
      List<ControlFieldType> controlFields =
          bibRecordType.get(BibConstants.BIB_RECORD_NUMBER).getControlfield();
      for (ControlFieldType controlField : controlFields) {
        VarField varFieldObjControlField = new VarField();
        varFieldObjControlField.setMarcTag(controlField.getTag());
        varFieldObjControlField.setContent(controlField.getValue());
        varFieldObjects.add(varFieldObjControlField);
      }
      varFieldObjects.add(varFieldObjLeader);
      return varFieldObjects;
    } catch (Exception e) {
      logger.error("FATAL-low - Unable to set varFields for " + bib.getNyplType() + " - "
          + bib.getId() + ", " + "source - " + bib.getNyplSource());
      logger.error("Error occurred while setting Bib's varfield properties - ", e);
      return null;
    }
  }

}
