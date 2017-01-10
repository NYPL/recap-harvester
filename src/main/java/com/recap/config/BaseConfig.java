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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.xml.models.BibRecord;

@Configuration
@PropertySource("classpath:application.properties")
public class BaseConfig {
	
	private static Logger logger = LoggerFactory.getLogger(BaseConfig.class);
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
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
