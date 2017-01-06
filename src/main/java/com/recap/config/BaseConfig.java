package com.recap.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.ListStreamsResult;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.utils.OAuth2Client;
import com.recap.xml.models.BibRecord;

@Configuration
@PropertySource("classpath:application.properties")
public class BaseConfig {
	
	@Value("${awsAccessKey}")
	private String awsAccessKey;
	
	@Value("${awsSecretKey}")
	private String awsSecretKey;
	
	@Value("${kinesisStream}")
	private String kinesisStream;
	
	private static Logger logger = LoggerFactory.getLogger(BaseConfig.class);
	
	@Bean
	public OAuth2Client oAuth2ClientNYPL(){
		return new OAuth2Client();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public AmazonKinesisClient getAmazonKinesisClient(){
		AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		AmazonKinesisClient amazonKinesisClient = new AmazonKinesisClient(awsCredentials);
		ListStreamsResult streamResults = amazonKinesisClient.listStreams();
		boolean foundStream = false;
		for(String streamName : streamResults.getStreamNames()){
			if(streamName.equals(kinesisStream)){
				foundStream = true;
				break;
			}
		}
		if(foundStream == false)
			amazonKinesisClient.createStream(kinesisStream, 2);
		
		logger.info("Configured Kinesis Client");
		return amazonKinesisClient;
	}
	
	@Bean
	public DataFormat getBibRecordJaxbDataFormat() throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(BibRecord.class);
		DataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
		logger.info("Set Dataformat to extract xml data based on xml element configured");
		return jaxbDataFormat;
	}
	
	@Bean
	public Map<String, String> locationCodeVals()
			throws JsonParseException, JsonMappingException, IOException{
		return new ObjectMapper().readValue(new File("src/main/resources/location.json"), Map.class);
	}
	
	@Bean
	public Map<String, Map<String, String>> materialCodeAndVals() 
			throws JsonParseException, JsonMappingException, IOException{
		return new ObjectMapper().readValue(new File("src/main/resources/materialLookup.json"), Map.class);
	}
	
	@Bean
	public Map<String, Map<String, String>> bibLevelCodeAndVals() 
			throws JsonParseException, JsonMappingException, IOException{
		return new ObjectMapper().readValue(new File("src/main/resources/bibLevelLookup.json"), Map.class);
	}
	
	@Bean
	public Map<String, String> countryCodeAndVals() 
			throws JsonParseException, JsonMappingException, IOException{
		return new ObjectMapper().readValue(new File("src/main/resources/countryLookup.json"), Map.class);
	}

}
