package com.recap.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.ListStreamsResult;
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
		return amazonKinesisClient;
	}
	
	@Bean
	public DataFormat getBibRecordJaxbDataFormat() throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(BibRecord.class);
		DataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
		return jaxbDataFormat;
	}

}
