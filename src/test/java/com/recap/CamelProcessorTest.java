package com.recap;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.bib.BibPublisher;
import com.recap.updater.bib.BibRecordProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.updater.holdings.ItemsPublisher;
import com.recap.utils.NyplApiUtil;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProcessor;
import com.recap.utils.TokenProperties;

public class CamelProcessorTest extends BaseTestCase {

	@Autowired
	CamelContext camelContext;

	ApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class);

	OAuth2Client nyplOAuth2Client = (OAuth2Client) context.getBean("oAuth2ClientNYPL");
	
	TokenProperties tokenProperties = nyplOAuth2Client.createAndGetTokenAccessProperties();

	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;

	@Value("${nyplApiForBibs}")
	private String nyplApiForBibs;
	
	@Value("${nyplApiForItems}")
	private String nyplApiForItems;

	@Test
	public void processSCSBExport() throws Exception {

		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("file:" + scsbexportstaging + "?fileName=onerecord.xml&noop=true")
				.split()
				.tokenizeXML("bibRecord")
				.process(new BibRecordProcessor()).process(new BibProcessor())
				.process(new BibJsonProcessor()).process(new Processor() {

					@Override
					public void process(Exchange exchange) throws Exception {
						exchange = setNyplApiUtilInExchange(exchange);
					}
				})
				.process(new BibPublisher())
				.process(new HoldingListProcessor())
				.process(new ItemsProcessor())
				.process(new Processor() {
	
					@Override
					public void process(Exchange exchange) throws Exception {
						exchange = setNyplApiUtilInExchange(exchange);
					}
				})
				.process(new ItemsPublisher());
			}
		});

		Thread.sleep(300000);

	}
	
	public Exchange setNyplApiUtilInExchange(Exchange exchange) throws Exception {
		tokenProperties = new TokenProcessor().validateAndReturnTokenProperties(tokenProperties, nyplOAuth2Client);
		NyplApiUtil apiUtil = new NyplApiUtil();
		apiUtil.setNyplApiForBibs(nyplApiForBibs);
		apiUtil.setNyplApiForItems(nyplApiForItems);
		apiUtil.setoAuth2Client(nyplOAuth2Client);
		apiUtil.setTokenProperties(tokenProperties);
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		exchangeContents.put(Constants.API_UTIL, apiUtil);
		exchange.getIn().setBody(exchangeContents);

		return exchange;
	}
}