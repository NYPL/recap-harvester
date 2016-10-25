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
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;


public class BibRecordProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		String xml = (String) exchange.getIn().getBody();
		BibRecord bibRecord = getBibRecord(xml);
		exchange.getIn().setBody(bibRecord);
	}
	
	private static Logger logger = Logger.getLogger(BibRecordProcessor.class);
	
	public BibRecord getBibRecord(String bibRecordXML) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(BibRecord.class);
		Unmarshaller UnMarshaller = jaxbContext.createUnmarshaller();
		BibRecord bibRecord = (BibRecord) UnMarshaller.unmarshal(new StringReader(bibRecordXML));
		return bibRecord;
	}

}
