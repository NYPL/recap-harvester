package com.recap.processor;

import java.util.Date;

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

import com.recap.exceptions.RecapHarvesterException;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProperties;

@Component
public class ReCapXmlRouteBuilderReceiver extends RouteBuilder{
	
	private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilderReceiver.class);
	
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
	private TokenProperties tokenProperties;
	
	@Override
	public void configure() throws Exception {
		onException(RecapHarvesterException.class)
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, 
						Throwable.class);
				logger.error("RECAPHARVESTER ERROR HANDLED - ", caught);
			}
		})
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setHeader("rabbitmq.ROUTING_KEY", "failure");
			}
		})
		.to("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
		System.getenv("rabbitmq_port") + "/recap_xchange?"
				+ "connectionFactory=#rabbitConnectionFactory&"
				+ "queue=recap_failures&"
				+ "autoDelete=false&"
				+ "routingKey=failure&"
				+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
				+ "automaticRecoveryEnabled=true")
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
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getIn().setHeader("rabbitmq.ROUTING_KEY", "failure");
			}
		})
		.to("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
				System.getenv("rabbitmq_port") + "/recap_xchange?"
						+ "connectionFactory=#rabbitConnectionFactory&"
						+ "queue=recap_failures&"
						+ "autoDelete=false&"
						+ "routingKey=failure&"
						+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
						+ "automaticRecoveryEnabled=true")
		.handled(true);
		
		from("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
				System.getenv("rabbitmq_port") + "/recap_xchange?"
				+ "connectionFactory=#rabbitConnectionFactory&"
				+ "queue=recap_bibs&"
				+ "autoDelete=false&"
				+ "routingKey=recap_bibs&"
				+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
				+ "automaticRecoveryEnabled=true&"
				+ "prefetchCount=1")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				byte[] exchangeContent = (byte[]) exchange.getIn().getBody();
				String body = new String(exchangeContent);
				setAPIAccessInfoInExchange(exchange);
				logger.info("Published bib to api - " + body);
			}
		})
		.to(nyplApiForBibs);
		
		from("rabbitmq://"+System.getenv("rabbitmq_host") + ":" +
				System.getenv("rabbitmq_port") + "/recap_xchange?"
				+ "connectionFactory=#rabbitConnectionFactory&"
				+ "queue=recap_items&"
				+ "autoDelete=false&"
				+ "routingKey=recap_items&"
				+ "vhost=" + System.getenv("rabbitmq_vhost") + "&"
				+ "automaticRecoveryEnabled=true&"
				+ "prefetchCount=1")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws RecapHarvesterException {
				byte[] exchangeContent = (byte[]) exchange.getIn().getBody();
				String body = new String(exchangeContent);
				setAPIAccessInfoInExchange(exchange);
				logger.info("Published item to api - " + body);
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
