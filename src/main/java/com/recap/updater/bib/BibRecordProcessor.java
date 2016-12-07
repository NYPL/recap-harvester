package com.recap.updater.bib;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.recap.xml.models.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BibRecordProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		String xml = (String) exchange.getIn().getBody();
		BibRecord bibRecord = getBibRecord(xml);
		exchange.getIn().setBody(bibRecord);
	}
	
	private static Logger logger = LoggerFactory.getLogger(BibRecordProcessor.class);
	
	public BibRecord getBibRecord(String bibRecordXML) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(BibRecord.class);
		Unmarshaller UnMarshaller = jaxbContext.createUnmarshaller();
		BibRecord bibRecord = (BibRecord) UnMarshaller.unmarshal(new StringReader(bibRecordXML));
		return bibRecord;
	}

}
