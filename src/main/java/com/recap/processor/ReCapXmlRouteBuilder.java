package com.recap.processor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.bib.BibPublisher;
import com.recap.updater.bib.BibRecordProcessor;
import com.recap.utils.models.OAuth2Client;
import com.recap.utils.models.TokenProperties;

@Component
public class ReCapXmlRouteBuilder implements RoutesBuilder {

	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;

	@Value("${nyplApiForBibs}")
	private String nyplApiForBibs;

	ApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class);
	
	OAuth2Client nyplOAuth2Client = (OAuth2Client) context.getBean("oAuth2ClientNYPL");
	
	TokenProperties tokenProperties = nyplOAuth2Client.createAndGetTokenAccessProperties();
	
	private static Logger logger = Logger.getLogger(ReCapXmlRouteBuilder.class);

	@Override
	public void addRoutesToCamelContext(CamelContext camelContext) throws Exception {
		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("file:" + scsbexportstaging + "?fileName=recapSample.xml&noop=true")
				.split()
				.tokenizeXML("bibRecord")
				.process(new BibRecordProcessor())
				.process(new BibProcessor())
				.process(new BibJsonProcessor())
				.process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						Date currentDate = new Date();
						int minutes = currentDate.getMinutes();
						currentDate.setMinutes(minutes + 5);
						if (!currentDate.before(tokenProperties.getTokenExpiration())) {
							logger.info("Requesting new nypl token");
							tokenProperties = nyplOAuth2Client.createAndGetTokenAccessProperties();
						}
						logger.info("Going to send bib to API Service at - "
								+ new SimpleDateFormat("yyyy-MM-dd").format(currentDate));
						logger.info("Token expires - " + tokenProperties.getTokenExpiration());
						logger.info(tokenProperties.getTokenValue());
					}
				})
				.process(new BibPublisher(nyplApiForBibs, nyplOAuth2Client, tokenProperties));
			}
		});
	}

}
