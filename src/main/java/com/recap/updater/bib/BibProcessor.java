package com.recap.updater.bib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.models.SubField;
import com.recap.models.VarField;
import com.recap.xml.models.BibRecord;
import com.recap.xml.models.DataFieldType;
import com.recap.xml.models.RecordType;
import com.recap.xml.models.SubfieldatafieldType;

public class BibProcessor implements Processor{
	
	private static Logger logger = Logger.getLogger(BibProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
		Bib bib = getBibFromBibRecord(bibRecord);
		Map<String, Object> exchangeContents = new HashMap<>();
		exchangeContents.put(Constants.BIB, bib);
		exchangeContents.put(Constants.BIB_RECORD, bibRecord);
		exchange.getIn().setBody(exchangeContents);
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

}
