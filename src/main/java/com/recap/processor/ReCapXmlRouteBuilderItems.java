package com.recap.processor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.bib.BibRecordProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.updater.holdings.ItemsJsonProcessor;
import com.recap.xml.models.BibRecord;

@Component
public class ReCapXmlRouteBuilderItems extends RouteBuilder{
	
	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;
	
	@Value("${kinesisStream}")
	private String kinesisStream;
	
	@Value("${itemShard}")
	private int itemShard;
	
	List<String> itemsProcessed = new ArrayList<String>();
	
	private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilderItems.class);

	@Override
	public void configure() throws Exception {
		from("file:" + scsbexportstaging + "?noop=true")
		.split()
		.tokenizeXML("bibRecord")
		.process(new BibRecordProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				Map<String, Object> exchangeContents = new HashMap<>();
				BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
				exchangeContents.put(Constants.BIB_RECORD, bibRecord);
				exchange.getIn().setBody(exchangeContents);
			}
		})
		.process(new HoldingListProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				Map<String, Object> exchangeContents = (Map<String, Object>)
						exchange.getIn().getBody();
				Bib bib = new BibProcessor().getBibFromBibRecord(
						(BibRecord) exchangeContents.get(Constants.BIB_RECORD));
				exchangeContents.put(Constants.BIB, bib);
				exchange.getIn().setBody(exchangeContents);
			}
		})
		.process(new ItemsProcessor())
		.process(new ItemsJsonProcessor())
		.split(body())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				System.out.println(exchange.getIn().getBody());
				Map<String, Object> keyValues = getMappedResults(
						(String)exchange.getIn().getBody());
				List<String> bibsIds = (List<String>) keyValues.get("bibIds");
				StringBuilder checkVal = new StringBuilder();
				String itemId = (String) keyValues.get("id");
				checkVal.append(itemId);
				checkVal.append(" - ");
				checkVal.append(bibsIds.get(0));
				String finalCheckValue = checkVal.toString();
				if(itemsProcessed.contains(finalCheckValue))
					logger.info("####################\n" + 
				"REPEATED - " + finalCheckValue + "\n####################\n");
				else
					itemsProcessed.add(finalCheckValue);
				ByteBuffer byteBuffer = ByteBuffer.wrap(
						((String)exchange.getIn().getBody()).getBytes());
				exchange.getIn().setBody(byteBuffer);		
				exchange.getIn().setHeader(Constants.PARTITION_KEY, itemShard);
				exchange.getIn().setHeader(Constants.SEQUENCE_NUMBER, 
						System.currentTimeMillis());
			}
		})
		.to("aws-kinesis://" + kinesisStream 
				+ "?amazonKinesisClient=#getAmazonKinesisClient");
	}
	
	private Map<String, Object> getMappedResults(String exchangeContent) throws JsonParseException, JsonMappingException, IOException{
		 Map<String, Object> keyValues = new ObjectMapper().
				 readValue(exchangeContent, Map.class);
		 return keyValues;
	}

}
