package com.recap.updater.bib;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.xml.models.BibRecord;

public class BibJsonProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> bibAndBibRecord = (Map<String, Object>) exchange.getIn().getBody();
		Bib bib = (Bib) bibAndBibRecord.get(Constants.BIB);	
		BibRecord bibRecord = (BibRecord) bibAndBibRecord.get(Constants.BIB_RECORD);
		String bibInJson = getBibAsJSON(bib);
		Map<String, Object> jsonBibAndBibRecord = new HashMap<>();
		jsonBibAndBibRecord.put(Constants.BIB_JSON, bibInJson);
		jsonBibAndBibRecord.put(Constants.BIB_RECORD, bibRecord);
		exchange.getIn().setBody(jsonBibAndBibRecord);
	}
	
	
	public String getBibAsJSON(Bib bib) throws JsonProcessingException {
		 return new ObjectMapper().writeValueAsString(bib);
	}

}
