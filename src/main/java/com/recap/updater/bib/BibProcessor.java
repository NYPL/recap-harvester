package com.recap.updater.bib;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.models.SubField;
import com.recap.models.VarField;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.ControlFieldType;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.RecordType;
import com.recap.xml.models.SubfieldatafieldType;

@Component
public class BibProcessor implements Processor{
	
	private BaseConfig baseConfig;
	
	private static Logger logger = LoggerFactory.getLogger(BibProcessor.class);
	
	public BibProcessor(BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
		Bib bib = getBibFromBibRecord(bibRecord);

		logger.info("Processing bib - " + bib.getId());
		exchange.getIn().setBody(bib);
	}
	
	public Bib getBibFromBibRecord(BibRecord bibRecord) throws Exception{
		try{
			Bib bib = new Bib();
			String bibId;
			String originalBibIdFromRecap = bibRecord.getBib().getOwningInstitutionBibId();
			if(originalBibIdFromRecap.startsWith(".")){
				bibId = originalBibIdFromRecap.substring(2, originalBibIdFromRecap.length() - 1);
			}else
				bibId = bibRecord.getBib().getOwningInstitutionBibId();
			bib.setId(bibId);
			bib.setNyplSource("recap-" + bibRecord.getBib().getOwningInstitutionId());
			bib.setNyplType("bib");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			bib.setUpdatedDate(dateFormat.format(new Date()));
			bib.setDeleted(false);
			bib.setSuppressed(false);
			List<RecordType> bibRecordType = bibRecord.getBib().getContent().getCollection().
					getRecord();
			if(bibRecordType.size() == 1){
				bib.setTitle(getTitle(bibRecordType));
				bib.setAuthor(getAuthor(bibRecordType));
				bib.setLang(getLanguageField(bibRecordType));
				bib.setMaterialType(getMaterialType(bibRecordType));
				bib.setVarFields(getVarFields(bibRecordType));
			}
			return bib;
		}catch(Exception e){
			logger.error("Error occurred while setting Bib properties - ", e);
			throw new Exception(e.getMessage());
		}
	}
	
	public String getLanguageField(List<RecordType> bibRecordType)
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
					return baseConfig.locationCodeVals().get(langCode.trim());
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
		System.out.println(baseConfig.materialCodeAndVals().get("c"));
		Map<String, String> materialCodeVal = baseConfig.materialCodeAndVals()
				.get(Character.toString(stringBuffer.toString().charAt(0)));
		Map<String, String> materialTypeInSierraFormat = new HashMap<>();
		materialTypeInSierraFormat.put("code", Character.toString(stringBuffer.toString()
				.charAt(0)));
		materialTypeInSierraFormat.put("value", materialCodeVal.get("sierraLabel"));
		return materialTypeInSierraFormat;
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
