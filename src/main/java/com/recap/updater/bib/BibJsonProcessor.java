package com.recap.updater.bib;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.constants.Constants;
import com.recap.models.Bib;

public class BibJsonProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		Bib bib = (Bib) exchangeContents.get(Constants.BIB);	
		String bibInJson = getBibAsJSON(bib);
		exchangeContents.put(Constants.BIB_JSON, bibInJson);
		exchange.getIn().setBody(exchangeContents);
	}
	
	
	public String getBibAsJSON(Bib bib) throws JsonProcessingException {
		 return new ObjectMapper().writeValueAsString(bib);
	}

}
