package com.recap.config;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.xml.models.BibRecord;

@Configuration
@PropertySource("classpath:application.properties")
public class BaseConfig {

  private static Logger logger = LoggerFactory.getLogger(BaseConfig.class);

  @Autowired
  private ResourceLoader resourceLoader;

  @Bean
  public DataFormat getBibRecordJaxbDataFormat() throws RecapHarvesterException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(BibRecord.class);
      DataFormat jaxbDataFormat = new JaxbDataFormat(String.valueOf(jaxbContext));
      logger.info("Set Dataformat to extract xml data based on xml element configured");
      return jaxbDataFormat;
    } catch (JAXBException jaxbException) {
      logger.error("XML file processing Error - JAXBException occurred - ", jaxbException);
      throw new RecapHarvesterException(
          "Hit a JAXBException during bean configuration " + jaxbException.getMessage());
    }
  }

  @Bean
  public Map<String, String> locationCodeVals()
      throws JsonParseException, JsonMappingException, IOException {
    return new ObjectMapper().readValue(
        resourceLoader.getResource("classpath:location.json").getInputStream(), Map.class);
  }

  @Bean
  public Map<String, Map<String, String>> materialCodeAndVals()
      throws JsonParseException, JsonMappingException, IOException {
    return new ObjectMapper().readValue(
        resourceLoader.getResource("classpath:materialLookup.json").getInputStream(), Map.class);
  }

  @Bean
  public Map<String, Map<String, String>> bibLevelCodeAndVals()
      throws JsonParseException, JsonMappingException, IOException {
    return new ObjectMapper().readValue(
        resourceLoader.getResource("classpath:bibLevelLookup.json").getInputStream(), Map.class);
  }

  @Bean
  public Map<String, String> countryCodeAndVals()
      throws JsonParseException, JsonMappingException, IOException {
    return new ObjectMapper().readValue(
        resourceLoader.getResource("classpath:countryLookup.json").getInputStream(), Map.class);
  }

  @Bean
  public AmazonKinesisClient getAmazonKinesisClient() {
    AmazonKinesisClient amazonKinesisClient = new AmazonKinesisClient();

    logger.info("Configured Kinesis Client");

    return amazonKinesisClient;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(60000);
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(100);
    retryTemplate.setBackOffPolicy(backOffPolicy);
    retryTemplate.setRetryPolicy(retryPolicy);
    return retryTemplate;
  }

}
