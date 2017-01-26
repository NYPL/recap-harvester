package com.recap.updater.bib;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Bib;

public class BibJsonProcessor implements Processor{
	
	private static Logger logger = LoggerFactory.getLogger(BibJsonProcessor.class);

	@Override
	public void process(Exchange exchange) throws RecapHarvesterException{
		Bib bib = (Bib) exchange.getIn().getBody();	
		String bibInJson = getBibAsJSON(bib);
		exchange.getIn().setBody(bibInJson);
	}
	
	
	public String getBibAsJSON(Bib bib) throws RecapHarvesterException {
		try{
			return new ObjectMapper().writeValueAsString(bib);
		}catch(JsonProcessingException jsonProcessingException){
			logger.error("JsonProcessingException occurred - ", jsonProcessingException);
			throw new RecapHarvesterException("Exception occured while trying to convert "
					+ "bib to json format" + jsonProcessingException.getMessage());
		}
	}

}
