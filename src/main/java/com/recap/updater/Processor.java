package com.recap.updater;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.models.Bib;
import com.recap.models.SubField;
import com.recap.models.VarField;
import com.recap.xml.models.*;
import org.apache.log4j.Logger;


public class Processor {
	
	private static Logger logger = Logger.getLogger(Processor.class);
	
	public List<BibRecord> getBibRecords(String xmlContents) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(BibRecords.class);
		Unmarshaller UnMarshaller = jaxbContext.createUnmarshaller();
		BibRecords bibRecords = (BibRecords) UnMarshaller.unmarshal(new StringReader(xmlContents));
		List<BibRecord> listBibRecords = ((BibRecords) bibRecords).getBibRecords();
		return listBibRecords;
	}
	
	public Bib getBibFromBibRecord(BibRecord bibRecord) throws Exception{
		try{
			Bib bib = new Bib();
			bib.setId(bibRecord.getBib().getOwningInstitutionId() + "-" + 
			bibRecord.getBib().getOwningInstitutionBibId());
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			bib.setUpdatedDate(dateFormat.format(new Date()));
			bib.setDeleted(false);
			bib.setSuppressed(false);
			List<RecordType> bibRecordType = bibRecord.getBib().getContent().getCollection().
					getRecord();
			List<VarField> varFieldObjects = new ArrayList<>();
			if(bibRecordType.size() == 1){
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
				bib.setVarFields(varFieldObjects);
			}
			return bib;
		}catch(Exception e){
			logger.error("Error occurred while setting Bib properties - ", e);
			throw new Exception(e.getMessage());
		}
	}
	
	public List<String> getListOfBibsAsJSON(List<Bib> bibs) throws JsonProcessingException {
		List<String> jsonBibs = new ArrayList<>();
		for(Bib bib : bibs){
			jsonBibs.add(new ObjectMapper().writeValueAsString(bib));
		}
		return jsonBibs;
	}
	
}
