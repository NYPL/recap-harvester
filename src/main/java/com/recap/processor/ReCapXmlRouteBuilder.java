package com.recap.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
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

@Component
public class ReCapXmlRouteBuilder implements RoutesBuilder {

	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;

	@Value("${nyplApiForBibs}")
	private String nyplApiForBibs;

	ApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class);

	OAuth2Client nyplOAuth2Client = (OAuth2Client) context.getBean("oAuth2ClientNYPL");

	@Override
	public void addRoutesToCamelContext(CamelContext camelContext) throws Exception {
		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("file:" + scsbexportstaging + "?fileName=recapSample.xml&noop=true")
				.split()
				.tokenizeXML("bibRecord").process(new BibRecordProcessor()).process(new BibProcessor())
				.process(new BibJsonProcessor())
				.process(new BibPublisher(nyplApiForBibs, nyplOAuth2Client,
								nyplOAuth2Client.getTokenAccessProperties()));
			}
		});
	}

}
