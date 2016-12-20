package com.recap.processor;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.bib.BibRecordProcessor;
import com.recap.utils.NyplApiUtil;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProcessor;
import com.recap.utils.TokenProperties;

//@Component
public class ReCapXmlRouteBuilderBibs extends RouteBuilder {

	@Value("${scsbexportstaging.location}")
	private String scsbexportstaging;

	@Value("${nyplApiForBibs}")
	private String nyplApiForBibs;
	
	@Value("${nyplApiForItems}")
	private String nyplApiForItems;
	
	@Value("${kinesisStream}")
	private String kinesisStream;
	
	@Value("{$bibShard}")
	private String bibShard;

	private ApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class);

	private OAuth2Client nyplOAuth2Client = (OAuth2Client) context.getBean("oAuth2ClientNYPL");

	private TokenProperties tokenProperties = nyplOAuth2Client.createAndGetTokenAccessProperties();
	
	@Override
	public void configure() throws Exception {
		from("file:" + scsbexportstaging + "?noop=true")
		.split(body().tokenizeXML("bibRecord", ""))
		.streaming()
		.process(new BibRecordProcessor()).process(new BibProcessor())
		.process(new BibJsonProcessor())
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				String body = (String) exchange.getIn().getBody();
				ByteBuffer byteBuffer = ByteBuffer.wrap(body.getBytes());
				exchange.getIn().setBody(byteBuffer);
				exchange.getIn().setHeader(Constants.PARTITION_KEY, 1);
				exchange.getIn().setHeader(Constants.SEQUENCE_NUMBER, 
						System.currentTimeMillis());
			}
		})
		.to("aws-kinesis://" + kinesisStream 
				+ "?amazonKinesisClient=#getAmazonKinesisClient");
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
