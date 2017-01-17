package com.recap.processor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProperties;
import com.recap.updater.holdings.ItemsJsonProcessor;
import com.recap.xml.models.BibRecord;

@Component
public class ReCapXmlRouteBuilderItems extends RouteBuilder{
	
	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;

	@Value("${nyplApiForItems}")
	private String nyplApiForItems;
	
	@Value("${accessTokenUrl}")
	private String accessTokenUri;
	
	@Value("${clientId}")
	private String clientId;
	
	@Value("${clientSecret}")
	private String clientSecret;
	
	@Value("${grantType}")
	private String grantType;

	
	List<String> itemsProcessed = new ArrayList<String>();
	
	@Autowired
	private BaseConfig baseConfig;
	
	@Autowired
	private TokenProperties tokenProperties;
	
	private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilderItems.class);

	@Override
	public void configure() throws Exception {
		onException(Exception.class)
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, 
						Throwable.class);
				logger.error("FATAL ERROR - ", caught);
			}
		})
		.handled(true);
		
		
		from("file:" + scsbexportstaging + "?fileName=testrecord.xml&"
				+ "maxMessagesPerPoll=1&noop=true")
		.split(body().tokenizeXML("bibRecord", ""))
		.streaming()
		.unmarshal("getBibRecordJaxbDataFormat")
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
				Bib bib = new BibProcessor(baseConfig).getBibFromBibRecord(
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
				String body = (String) exchange.getIn().getBody();
				logger.info(body);
				exchange.getIn().setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST));
				exchange.getIn().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
				exchange.getIn().setHeader("Authorization", "Bearer " + getToken());
				HttpServletRequest request = exchange.getIn().getBody(HttpServletRequest.class);
				exchange.getIn().setHeader(Exchange.HTTP_SERVLET_REQUEST, request);
			}
		})
		.to(nyplApiForItems);
	}
	
	public String getToken() throws Exception {
		Date currentDate = new Date();
		currentDate.setMinutes(currentDate.getMinutes() + 5);
		if(tokenProperties.getTokenExpiration() == null || 
				!currentDate.before(tokenProperties.getTokenExpiration())){
			logger.info("Requesting new nypl token");
			tokenProperties = new OAuth2Client(
					accessTokenUri, clientId, clientSecret, grantType)
					.createAndGetTokenAccessProperties();
			return tokenProperties.getTokenValue();
		}
		logger.info("Going to send bib to API Service at - "
				+ new SimpleDateFormat("yyyy-MM-dd").format(currentDate));
		logger.info("Token expires - " + tokenProperties.getTokenExpiration());
		logger.info(tokenProperties.getTokenValue());
		return tokenProperties.getTokenValue();
	}
}
