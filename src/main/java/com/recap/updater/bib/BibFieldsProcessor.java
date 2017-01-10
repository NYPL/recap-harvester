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
import com.recap.models.Bib;
import com.recap.models.SubField;
import com.recap.models.VarField;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.ControlFieldType;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.RecordType;
import com.recap.xml.models.SubfieldatafieldType;

public class BibFieldsProcessor {
	
	private BaseConfig baseConfig;
	
	private static Logger logger = LoggerFactory.getLogger(BibFieldsProcessor.class);
	
	public BibFieldsProcessor(BaseConfig baseConfig){
		this.baseConfig = baseConfig;
	}
	
	public Map<String, String> getLanguageField(List<RecordType> bibRecordType)
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> langCodeName = new LinkedHashMap<>();
		String langCode = getLanguageCode(bibRecordType);
		try{
			langCodeName.put(Constants.CODE, langCode);
			langCodeName.put(Constants.NAME, baseConfig.locationCodeVals().get(langCode));
		}catch(NullPointerException nullPointerException){
			langCodeName.put(Constants.CODE, langCode);
			langCodeName.put(Constants.NAME, null);
		}
		return langCodeName;
	}
	
	public String getLanguageCode(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		List<ControlFieldType> controlFields = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getControlfield();
		StringBuffer lang = new StringBuffer();
		boolean isProcessed = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals(
					Constants.CONTROL_FIELD_TYPE_008) && 
					!isProcessed){
				try{
					lang.append(controlFieldType.getValue());
					String langCode = lang.substring(
							Constants.CONTROL_FIELD_LANG_CODE_START_INDEX, 
							Constants.CONTROL_FIELD_LANG_CODE_END_INDEX);
					isProcessed = true;
					return langCode.trim();
				}catch (StringIndexOutOfBoundsException stringOutOfBounds){
					logger.error("FATAL-low - Unable to get Language from - " + 
				controlFieldType.getValue());
				}
			}
		}
		return null;
	}
	
	public String getTitle(List<RecordType> bibRecordType){
		boolean isProcessed = false;
		for(DataFieldType dataFieldType : bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getDatafield()){
			if(dataFieldType.getTag().equals(
					Constants.DATAFIELD_TYPE_TITLE_CODE) && !isProcessed){
				StringBuffer stringBuffer = new StringBuffer();
				for(SubfieldatafieldType subField : dataFieldType.getSubfield()){
					stringBuffer.append(subField.getValue() + " ");
				}
				isProcessed = true;
				return stringBuffer.toString().trim();
			}
		}
		return null;
	}
	
	public String getAuthor(List<RecordType> bibRecordType){
		boolean isProcessed = false;
		for(DataFieldType dataFieldType : bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getDatafield()){
			if(dataFieldType.getTag().equals(
					Constants.DATAFIELD_TYPE_AUTHOR_CODE) && 
					!isProcessed){
				StringBuffer stringBuffer = new StringBuffer();
				for(SubfieldatafieldType subField : dataFieldType.getSubfield()){
					if(subField.getCode().equals(
							Constants.SUBFIELD_TYPE_AUTHOR_CODE))
						stringBuffer.append(subField.getValue() + " ");
				}
				isProcessed = true;
				return stringBuffer.toString().trim();
			}
		}
		return null;
	}
	
	public Map<String, String> getMaterialType(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> materialTypeInSierraFormat = new LinkedHashMap<>();
		List<ControlFieldType> controlFields = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getControlfield();
		boolean isProcessedControlField = false;
		boolean bookTextMaterialType = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals( Constants.CONTROL_FIELD_TYPE_007) &&
					!isProcessedControlField ){
				if(controlFieldType.getValue().startsWith(Constants.REPR_OF_MICROFORM)){
					materialTypeInSierraFormat.put(
							Constants.CODE, Constants.REPR_OF_MICROFORM);
					materialTypeInSierraFormat.put(
							Constants.VALUE, Constants.MICROFORM);
					isProcessedControlField = true;
					return materialTypeInSierraFormat;
				}else if(controlFieldType.getValue().startsWith(" ")){
					bookTextMaterialType = true;
					isProcessedControlField = true;
				}
			}
		}
		
		List<DataFieldType> dataFields = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getDatafield();
		boolean isProcessedDataField = false;
		for(DataFieldType dataFieldType : dataFields){
			if(dataFieldType.getTag().equals(
					Constants.DATAFIELD_TYPE_337) && !isProcessedDataField){
				List<SubfieldatafieldType> subFields = dataFieldType.getSubfield();
				for(SubfieldatafieldType subField : subFields){
					if(subField.getCode().equals(Constants.SUBFIELD_TYPE_b) &&
							!isProcessedDataField) {
						if(subField.getValue().equals(Constants.REPR_OF_MICROFORM)){
							materialTypeInSierraFormat.put(Constants.CODE, Constants.REPR_OF_MICROFORM);
							materialTypeInSierraFormat.put(Constants.VALUE, Constants.MICROFORM);
							isProcessedDataField = true;
							return materialTypeInSierraFormat;
						}else if(subField.getValue().equals(" ")){
							bookTextMaterialType = true;
							isProcessedDataField = true;
						}
					}
							 
				}
			}
		}
		String leaderValue = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getLeader().getValue();
		if(Character.toString(leaderValue.charAt(6)).equals(" ") && bookTextMaterialType){
			materialTypeInSierraFormat.put(Constants.CODE, Constants.REPR_OF_BOOK_TEXT);
			materialTypeInSierraFormat.put(Constants.VALUE, Constants.BOOK_TEXT);
			return materialTypeInSierraFormat;
		}
		
		Map<String, String> materialCodeVal = baseConfig.materialCodeAndVals()
				.get(Character.toString(leaderValue.charAt(6)));
		materialTypeInSierraFormat.put(Constants.CODE, 
				Character.toString(leaderValue.charAt(6)));
		try{
			materialTypeInSierraFormat.put(Constants.VALUE, materialCodeVal.get(
					Constants.SIERRA_LABEL));
		}catch(NullPointerException nullPointerException){
			materialTypeInSierraFormat = null;
		}
		
		return materialTypeInSierraFormat;
	}
	
	public Map<String, String> getBibLevel(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		String leaderValue = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getLeader().getValue();
		StringBuffer stringBuffer = new StringBuffer(leaderValue.substring(7));
		Map<String, String> materialCodeVal = baseConfig.bibLevelCodeAndVals()
				.get(Character.toString(stringBuffer.toString().charAt(0)));
		Map<String, String> materialTypeInSierraFormat = new HashMap<>();
		materialTypeInSierraFormat.put(Constants.CODE, Character.toString(stringBuffer.toString()
				.charAt(0)));
		try{
			materialTypeInSierraFormat.put(Constants.VALUE, materialCodeVal.get(
					Constants.SIERRA_LABEL));
		}catch(NullPointerException nullPointerException){
			materialTypeInSierraFormat = null;
		}
		
		return materialTypeInSierraFormat;
	}
	
	public Integer getPublishYear(List<RecordType> bibRecordType)
			throws JsonParseException, JsonMappingException, IOException{
		List<ControlFieldType> controlFields = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getControlfield();
		StringBuffer publishYear = new StringBuffer();
		boolean isProcessed = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals(
					Constants.CONTROL_FIELD_TYPE_008) && !isProcessed){
				try{
					publishYear.append(controlFieldType.getValue());
					Integer publishYearNumber = Integer.parseInt(
							publishYear.substring(
									Constants.CONTROL_FIELD_PUBLISH_YEAR_START_INDEX, 
									Constants.CONTROL_FIELD_PUBLISH_YEAR_END_INDEX).trim());
					isProcessed = true;
					return publishYearNumber;
				}catch (StringIndexOutOfBoundsException stringOutOfBounds){
					logger.error("FATAL-low - Unable to get publishYear from - " + 
				controlFieldType.getValue());
				}catch(NumberFormatException numberFormatException){
					logger.error("FATAL-low - Unable to get publishYear from - " + 
							controlFieldType.getValue());
				}
			}
		}
		return null;
	}
	
	public Map<String, String> getCountry(List<RecordType> bibRecordType)
			throws JsonParseException, JsonMappingException, IOException{
		List<ControlFieldType> controlFields = bibRecordType.get(
				Constants.BIB_RECORD_NUMBER).getControlfield();
		StringBuffer countryInfo = new StringBuffer();
		boolean isProcessed = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals(Constants.CONTROL_FIELD_TYPE_008) && !isProcessed){
				try{
					countryInfo.append(controlFieldType.getValue());
					String countryCode = countryInfo.substring(
							Constants.CONTROL_FIELD_COUNTRY_CODE_START_INDEX, 
							Constants.CONTROL_FIELD_COUNTRY_CODE_END_INDEX).trim();
					String countryName = baseConfig.countryCodeAndVals().get(countryCode);
					isProcessed = true;
					Map<String, String> countryCodeName = new HashMap<>();
					countryCodeName.put(Constants.CODE, countryCode);
					countryCodeName.put(Constants.NAME, countryName);
					if(countryName == null)
						return null;
					return countryCodeName;
				}catch (StringIndexOutOfBoundsException stringOutOfBounds){
					logger.error("FATAL-low - Unable to get publishYear from - " + 
				controlFieldType.getValue());
				}catch(NumberFormatException numberFormatException){
					logger.error("FATAL-low - Unable to get publishYear from - " + 
							controlFieldType.getValue());
				}
			}
		}
		return null;
	}
	
	public Map<String, Map<String, String>> getFixedFields(BibRecord bibRecord, 
			List<RecordType> bibRecordType, String updatedDate) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, Map<String, String>> fixedFields = new HashMap<>();
		fixedFields.put("24", getFixedFieldsForLanguage(bibRecordType));
		fixedFields.put("25", getFixedFieldsCharsSkipForTitle());
		fixedFields.put("26", getFixedFieldsLocation(bibRecord));
		fixedFields.put("27", getFixedFieldsCopies());
		fixedFields.put("28", getFixedFieldsForCatalogDate());
		fixedFields.put("29", getFixedFieldsForBibLevel(bibRecordType));
		fixedFields.put("30", getFixedFieldsForMaterialType(bibRecordType));
		fixedFields.put("31", getFixedFieldsForBibCode3());
		fixedFields.put("80", getFixedFieldsRecordType());
		fixedFields.put("81", getFixedFieldsForRecordNumber(bibRecord));
		fixedFields.put("83", getFixedFieldsCreatedDate());
		fixedFields.put("84", getFixedFieldsForUpdatedDate(updatedDate));
		fixedFields.put("85", getFixedFieldsNumberOfRevisions());
		fixedFields.put("86", getFixedFieldsAgency());
		fixedFields.put("89", getFixedFieldsForCountry(bibRecordType));
		fixedFields.put("98", getFixedFieldsPDate());
		fixedFields.put("107", getFixedFieldsMARCType());
		return fixedFields;
	}
	
	public Map<String, String> getFixedFieldsForLanguage(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> langCodeAndName = getLanguageField(bibRecordType);
		if(langCodeAndName == null)
			return null;
		Map<String, String> languageKeysAndVals = new LinkedHashMap<>();
		languageKeysAndVals.put(Constants.LABEL, Constants.LANGUAGE);
		languageKeysAndVals.put(Constants.VALUE, langCodeAndName.get(Constants.CODE));
		languageKeysAndVals.put(Constants.DISPLAY, langCodeAndName.get(Constants.NAME));
		return languageKeysAndVals;
	}
	
	public Map<String, String> getFixedFieldsCharsSkipForTitle(){
		Map<String, String> charsSkip = new LinkedHashMap<>();
		charsSkip.put(Constants.LABEL, Constants.SKIP);
		charsSkip.put(Constants.VALUE, Constants.SKIP_VALUE);
		return charsSkip;
	}
	
	public Map<String, String> getFixedFieldsForBibLevel(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> bibLevelInfo = getBibLevel(bibRecordType);
		if(bibLevelInfo == null)
			return null;
		Map<String, String> fixedFieldsBibLevel = new LinkedHashMap<>();
		fixedFieldsBibLevel.put(Constants.LABEL, Constants.BIB_LEVEL);
		fixedFieldsBibLevel.put(Constants.VALUE, bibLevelInfo.get(Constants.CODE));
		fixedFieldsBibLevel.put(Constants.DISPLAY, bibLevelInfo.get(Constants.VALUE));
		return fixedFieldsBibLevel;
	}
	
	public Map<String, String> getFixedFieldsLocation(BibRecord bibRecord){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constants.RECAP_INITIALS);
		stringBuffer.append(bibRecord.getBib().getOwningInstitutionId().toLowerCase());
		Map<String, String> locationAndValue = new LinkedHashMap<>();
		locationAndValue.put(Constants.LABEL, Constants.LOCATION);
		locationAndValue.put(Constants.VALUE, stringBuffer.toString());
		return locationAndValue;
	}
	
	public Map<String, String> getFixedFieldsForMaterialType(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> materialTypeCodeAndVal = getMaterialType(bibRecordType);
		Map<String, String> materialTypeCodeVal = new LinkedHashMap<>();
		materialTypeCodeVal.put(Constants.LABEL, Constants.MATERIAL_TYPE);
		try{
			materialTypeCodeVal.put(Constants.VALUE, materialTypeCodeAndVal.get(Constants.CODE));
			materialTypeCodeVal.put(Constants.DISPLAY, materialTypeCodeAndVal.get(Constants.VALUE));
		}catch(NullPointerException nullPOinterException){
			materialTypeCodeAndVal = null;
		}
		return materialTypeCodeVal;
	}
	
	public Map<String, String> getFixedFieldsRecordType(){
		Map<String, String> recordTypeLabelAndVal = new LinkedHashMap<>();
		recordTypeLabelAndVal.put(Constants.LABEL, Constants.RECORD_TYPE);
		recordTypeLabelAndVal.put(Constants.VALUE, Constants.RECORD_TYPE_BIB);
		return recordTypeLabelAndVal;
	}
	
	public Map<String, String> getFixedFieldsForRecordNumber(BibRecord bibRecord){
		String bibId = bibRecord.getBib().getOwningInstitutionBibId();
		if(bibId.startsWith(".")){
			bibId = bibId.substring(2, bibId.length() - 1);
		}
		Map<String, String> recordNumberVal = new LinkedHashMap<>();
		recordNumberVal.put(Constants.LABEL, Constants.RECORD_NUMBER);
		recordNumberVal.put(Constants.VALUE, bibId);
		return recordNumberVal;
	}
	
	public Map<String, String> getFixedFieldsCreatedDate(){
		Map<String, String> createdDateVals = new LinkedHashMap<>();
		createdDateVals.put(Constants.LABEL, Constants.CREATED_DATE);
		createdDateVals.put(Constants.VALUE, null);
		return createdDateVals;
	}
	
	public Map<String, String> getFixedFieldsCopies(){
		Map<String, String> copies = new LinkedHashMap<>();
		copies.put(Constants.LABEL, Constants.COPIES);
		copies.put(Constants.VALUE, Constants.COPIES_VALUE);
		return copies;
	}
	
	public Map<String, String> getFixedFieldsForCatalogDate(){
		Map<String, String> catDate = new LinkedHashMap<>();
		catDate.put(Constants.LABEL, Constants.CATALOG_DATE);
		catDate.put(Constants.VALUE, null);
		return catDate;
	}
	
	public Map<String, String> getFixedFieldsForBibCode3(){
		Map<String, String> bibCode3Vals = new LinkedHashMap<>();
		bibCode3Vals.put(Constants.LABEL, Constants.BIB_CODE_3);
		bibCode3Vals.put(Constants.VALUE, null);
		return bibCode3Vals;
	}
	
	public Map<String, String> getFixedFieldsForUpdatedDate(String updatedDate){
		Map<String, String> updatedDateVals = new LinkedHashMap<>();
		updatedDateVals.put(Constants.LABEL, Constants.UPDATED_DATE);
		updatedDateVals.put(Constants.VALUE, updatedDate);
		return updatedDateVals;
	}
	
	public Map<String, String> getFixedFieldsNumberOfRevisions(){
		Map<String, String> numberOfRevisions = new LinkedHashMap<>();
		numberOfRevisions.put(Constants.LABEL, Constants.NUMBER_REVISIONS);
		numberOfRevisions.put(Constants.VALUE, Constants.NUMBER_REVISIONS_VALUE);
		return numberOfRevisions;
	}
	
	public Map<String, String> getFixedFieldsAgency(){
		Map<String, String> agencyVals = new LinkedHashMap<>();
		agencyVals.put(Constants.LABEL, Constants.AGENCY);
		agencyVals.put(Constants.VALUE, Constants.AGENCY_VALUE);
		return agencyVals;
	}
	
	public Map<String, String> getFixedFieldsForCountry(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> countryVals = new LinkedHashMap<>();
		Map<String, String> countryCodeName = getCountry(bibRecordType);
		if(countryCodeName == null) 
			return null;
		countryVals.put(Constants.LABEL, Constants.COUNTRY);
		countryVals.put(Constants.VALUE, countryCodeName.get(Constants.CODE));
		countryVals.put(Constants.DISPLAY, countryCodeName.get(Constants.NAME));
		return countryVals;
	}
	
	public Map<String, String> getFixedFieldsPDate(){
		Map<String, String> pDateVals = new LinkedHashMap<>();
		pDateVals.put(Constants.LABEL, Constants.PDATE);
		pDateVals.put(Constants.VALUE, null);
		return pDateVals;
	}
	
	public Map<String, String> getFixedFieldsMARCType(){
		Map<String, String> marcType = new LinkedHashMap<>();
		marcType.put(Constants.LABEL, Constants.MARC_TYPE);
		marcType.put(Constants.VALUE, " ");
		return marcType;
	}
	
	public List<VarField> getVarFields(List<RecordType> bibRecordType) throws Exception{
		try{
			List<VarField> varFieldObjects = new ArrayList<>();
			List<DataFieldType> varFields = bibRecordType.get(
					Constants.BIB_RECORD_NUMBER).getDatafield();
			for(DataFieldType varField : varFields){
				VarField varFieldObj = new VarField();
				varFieldObj.setFieldTag(varField.getId());
				varFieldObj.setInd1(varField.getInd1());
				varFieldObj.setInd2(varField.getInd2());
				varFieldObj.setMarcTag(varField.getTag());
				List<SubfieldatafieldType> subFields = varField.getSubfield();
				List<SubField> subFieldObjects = new ArrayList<>();
				for(SubfieldatafieldType subField : subFields){
					SubField subFieldObj = new SubField();
					subFieldObj.setContent(subField.getValue());
					subFieldObj.setTag(subField.getCode());
					subFieldObjects.add(subFieldObj);
				}
				varFieldObj.setSubFields(subFieldObjects);
				varFieldObjects.add(varFieldObj);
			}
			VarField varFieldObjLeader = new VarField();
			varFieldObjLeader.setFieldTag("_");
			varFieldObjLeader.setContent(bibRecordType.get(
					Constants.BIB_RECORD_NUMBER).getLeader().getValue());
			List<ControlFieldType> controlFields = bibRecordType.get(
					Constants.BIB_RECORD_NUMBER).getControlfield();
			for(ControlFieldType controlField : controlFields){
				VarField varFieldObjControlField = new VarField();
				varFieldObjControlField.setMarcTag(controlField.getTag());
				varFieldObjControlField.setContent(controlField.getValue());
				varFieldObjects.add(varFieldObjControlField);
			}
			varFieldObjects.add(varFieldObjLeader);
			return varFieldObjects;
		}catch(Exception e){
			logger.error("Error occurred while setting Bib's varfield properties - ", e);
			throw new Exception(e.getMessage());
		}
	}

}
