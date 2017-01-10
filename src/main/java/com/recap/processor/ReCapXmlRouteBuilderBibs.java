package com.recap.processor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProperties;

@Component
public class ReCapXmlRouteBuilderBibs extends RouteBuilder {

	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;

	@Value("${nyplApiForBibs}")
	private String nyplApiForBibs;
	
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
	
	private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilderBibs.class);
	
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
		
		from("file:" + scsbexportstaging + "?fileName=recapSampleForCUL.xml&noop=true")
		.split(body().tokenizeXML("bibRecord", ""))
		.streaming()
		.unmarshal("getBibRecordJaxbDataFormat")
		.process(new BibProcessor(baseConfig))
		.process(new BibJsonProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				String body = (String) exchange.getIn().getBody();
				System.out.println(body);
				exchange.getIn().setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST));
				exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
				exchange.getIn().setHeader("Authorization", "Bearer " + getToken());
				exchange.getIn().setBody(body);
			}
		})
		.to(nyplApiForBibs);
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
