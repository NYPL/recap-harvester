package com.recap.updater.bib;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.models.Bib;

public class BibJsonProcessor implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		Bib bib = (Bib) exchange.getIn().getBody();	
		String bibInJson = getBibAsJSON(bib);
		exchange.getIn().setBody(bibInJson);
	}
	
	
	public String getBibAsJSON(Bib bib) throws JsonProcessingException {
		 return new ObjectMapper().writeValueAsString(bib);
	}

}
