package com.recap.processor;

import java.util.Date;
import java.util.HashMap;
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

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Bib;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProperties;
import com.recap.updater.holdings.ItemsJsonProcessor;
import com.recap.xml.models.BibRecord;

@Component
public class ReCapXmlRouteBuilder extends RouteBuilder{
	
	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;
	
	@Value("${nyplApiForBibs}")
	private String nyplApiForBibs;

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
	
	@Autowired
	private BaseConfig baseConfig;
	
	@Autowired
	private TokenProperties tokenProperties;
	
	private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilder.class);

	@Override
	public void configure() {
		onException(RecapHarvesterException.class)
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, 
						Throwable.class);
				logger.error("RECAPHARVESTER ERROR HANDLED - ", caught);
			}
		})
		.handled(true);
		
		onException(Exception.class)
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, 
						Throwable.class);
				logger.error("APP FATAL UNEXPECTED ERROR - ", caught);
			}
		})
		.handled(true);
		
		from("file:" + scsbexportstaging + "?"
				+ "maxMessagesPerPoll=1&noop=true")
		.split(body().tokenizeXML("bibRecord", ""))
		.streaming()
		.unmarshal("getBibRecordJaxbDataFormat")
		.multicast()
		.to("direct:bib", "direct:item");
		
		
		from("direct:bib")
		.process(new BibProcessor(baseConfig))
		.process(new BibJsonProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				String body = (String) exchange.getIn().getBody();
				setAPIAccessInfoInExchange(exchange);
				logger.info(body);
			}
		})
		.to(nyplApiForBibs);
		
		from("direct:item")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				Map<String, Object> exchangeContents = new HashMap<>();
				BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
				exchangeContents.put(Constants.BIB_RECORD, bibRecord);
				exchange.getIn().setBody(exchangeContents);
			}
		})
		.process(new HoldingListProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
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
			public void process(Exchange exchange) throws RecapHarvesterException {
				String body = (String) exchange.getIn().getBody();
				setAPIAccessInfoInExchange(exchange);
				logger.info(body);
			}
		})
		.to(nyplApiForItems);
	}
	
	public void setAPIAccessInfoInExchange(Exchange exchange) throws RecapHarvesterException{
		exchange.getIn().setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST));
		exchange.getIn().setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		exchange.getIn().setHeader("Authorization", "Bearer " + getToken());
		HttpServletRequest request = exchange.getIn().getBody(HttpServletRequest.class);
		exchange.getIn().setHeader(Exchange.HTTP_SERVLET_REQUEST, request);
	}
	
	public String getToken() throws RecapHarvesterException {
		try{
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
			logger.info("Token expires - " + tokenProperties.getTokenExpiration());
			logger.info(tokenProperties.getTokenValue());
			return tokenProperties.getTokenValue();
		}catch(Exception e){
			logger.error("Exception caught - ", e);
			throw new RecapHarvesterException("Exception occurred while getting token");
		}
	}
}
