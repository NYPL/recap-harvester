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
import com.recap.models.SubField;
import com.recap.models.VarField;
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
	
	public String getLanguageField(List<RecordType> bibRecordType)
			throws JsonParseException, JsonMappingException, IOException{
		String langCode = getLanguageCode(bibRecordType);
		if(langCode != null)
			return baseConfig.locationCodeVals().get(langCode);
		else 
			return null;
	}
	
	public String getLanguageCode(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		List<ControlFieldType> controlFields = bibRecordType.get(0).getControlfield();
		StringBuffer lang = new StringBuffer();
		boolean isProcessed = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals("008") && isProcessed == false){
				try{
					lang.append(controlFieldType.getValue());
					String langCode = lang.substring(34, 38);
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
		for(DataFieldType dataFieldType : bibRecordType.get(0).getDatafield()){
			if(dataFieldType.getTag().equals("245") && !isProcessed){
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
		for(DataFieldType dataFieldType : bibRecordType.get(0).getDatafield()){
			if(dataFieldType.getTag().equals("100") && !isProcessed){
				StringBuffer stringBuffer = new StringBuffer();
				for(SubfieldatafieldType subField : dataFieldType.getSubfield()){
					if(subField.getCode().equals("a"))
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
		String leaderValue = bibRecordType.get(0).getLeader().getValue();
		StringBuffer stringBuffer = new StringBuffer(leaderValue.substring(6));
		Map<String, String> materialCodeVal = baseConfig.materialCodeAndVals()
				.get(Character.toString(stringBuffer.toString().charAt(0)));
		Map<String, String> materialTypeInSierraFormat = new HashMap<>();
		materialTypeInSierraFormat.put("code", Character.toString(stringBuffer.toString()
				.charAt(0)));
		materialTypeInSierraFormat.put("value", materialCodeVal.get("sierraLabel"));
		return materialTypeInSierraFormat;
	}
	
	public Map<String, String> getBibLevel(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		String leaderValue = bibRecordType.get(0).getLeader().getValue();
		StringBuffer stringBuffer = new StringBuffer(leaderValue.substring(7));
		Map<String, String> materialCodeVal = baseConfig.bibLevelCodeAndVals()
				.get(Character.toString(stringBuffer.toString().charAt(0)));
		Map<String, String> materialTypeInSierraFormat = new HashMap<>();
		materialTypeInSierraFormat.put("code", Character.toString(stringBuffer.toString()
				.charAt(0)));
		materialTypeInSierraFormat.put("value", materialCodeVal.get("sierraLabel"));
		return materialTypeInSierraFormat;
	}
	
	public Integer getPublishYear(List<RecordType> bibRecordType)
			throws JsonParseException, JsonMappingException, IOException{
		List<ControlFieldType> controlFields = bibRecordType.get(0).getControlfield();
		StringBuffer publishYear = new StringBuffer();
		boolean isProcessed = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals("008") && isProcessed == false){
				try{
					publishYear.append(controlFieldType.getValue());
					Integer publishYearNumber = Integer.parseInt(
							publishYear.substring(7, 11).trim());
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
		List<ControlFieldType> controlFields = bibRecordType.get(0).getControlfield();
		StringBuffer countryInfo = new StringBuffer();
		boolean isProcessed = false;
		for(ControlFieldType controlFieldType : controlFields){
			if(controlFieldType.getTag().equals("008") && isProcessed == false){
				try{
					countryInfo.append(controlFieldType.getValue());
					String countryCode = countryInfo.substring(14, 17).trim();
					String countryName = baseConfig.countryCodeAndVals().get(countryCode);
					isProcessed = true;
					Map<String, String> countryCodeName = new HashMap<>();
					countryCodeName.put("code", countryCode);
					countryCodeName.put("name", countryName);
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
	
	public Map<String, Map<String, String>> getFixedFields(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, Map<String, String>> fixedFields = new LinkedHashMap<>();
		fixedFields.put("24", getFixedFieldsForLanguage(bibRecordType));
		return fixedFields;
	}
	
	public Map<String, String> getFixedFieldsForLanguage(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> languageKeysAndVals = new LinkedHashMap<>();
		String language = getLanguageField(bibRecordType);
		if(language == null)
			return null;
		languageKeysAndVals.put("label", "Language");
		languageKeysAndVals.put("value", getLanguageCode(bibRecordType));
		languageKeysAndVals.put("display", language);
		return languageKeysAndVals;
	}
	
	public void getFixedFieldsForBibLevel(List<RecordType> bibRecordType) 
			throws JsonParseException, JsonMappingException, IOException{
		Map<String, String> bibLevelInfo = getBibLevel(bibRecordType);
		if(bibLevelInfo.get("value") == null)
			
			
	}
	
	public List<VarField> getVarFields(List<RecordType> bibRecordType) throws Exception{
		try{
			List<VarField> varFieldObjects = new ArrayList<>();
			List<DataFieldType> varFields = bibRecordType.get(0).getDatafield();
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
			varFieldObjLeader.setContent(bibRecordType.get(0).getLeader().getValue());
			List<ControlFieldType> controlFields = bibRecordType.get(0).getControlfield();
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
