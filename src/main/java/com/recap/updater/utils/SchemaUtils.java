package com.recap.updater.utils;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;

public class SchemaUtils {

  private static Logger logger = LoggerFactory.getLogger(SchemaUtils.class);

  private Exchange getExchangeWithAPIResponse(RetryTemplate retryTemplate,
      ProducerTemplate producerTemplate, String api) throws RecapHarvesterException {
    try {
      Exchange templateResultExchange =
          retryTemplate.execute(new RetryCallback<Exchange, RecapHarvesterException>() {

            @Override
            public Exchange doWithRetry(RetryContext context) throws RecapHarvesterException {
              try {
                return producerTemplate.request(api, new Processor() {
                  @Override
                  public void process(Exchange httpHeaderExchange) throws Exception {
                    httpHeaderExchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethod.GET);
                  }
                });
              } catch (Exception e) {
                logger.error("Error occurred while calling schema api - ", e);
                throw new RecapHarvesterException(
                    "Error occurred while calling schema api - " + e.getMessage());
              }
            }

          });
      return templateResultExchange;
    } catch (Exception e) {
      logger.error("Error occurred while calling schema api - ", e);
      throw new RecapHarvesterException(
          "Error occurred while calling schema api - " + e.getMessage());
    }
  }

  public String getSchema(RetryTemplate retryTemplate, ProducerTemplate producerTemplate,
      String api) throws RecapHarvesterException {
    try {
      Exchange exchange = getExchangeWithAPIResponse(retryTemplate, producerTemplate, api);
      Message out = exchange.getOut();

      HttpOperationFailedException httpOperationFailedException =
          exchange.getException(HttpOperationFailedException.class);

      Integer responseCode = null;
      String apiResponse = null;

      if (httpOperationFailedException != null) {
        responseCode = httpOperationFailedException.getStatusCode();
        apiResponse = httpOperationFailedException.getResponseBody();
        throw new RecapHarvesterException(
            "Failed to get schema: responseCode - " + responseCode + ", message -  " + apiResponse);
      } else {
        responseCode = out.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        apiResponse = out.getBody(String.class);
      }

      logger.info("schema api response code - " + responseCode);

      Map<String, Object> responseDeserialized =
          new ObjectMapper().readValue(apiResponse, Map.class);
      Map<String, Object> schemaData =
          (Map<String, Object>) responseDeserialized.get(Constants.SCHEMA_DATA);
      Map<String, String> schemaObject = 
          (Map<String, String>) schemaData.get("schemaObject");
      String schemaName = (String) schemaObject.get("name");
      logger.info("Successful schema api response for schema: " + schemaName);
      return (String) schemaData.get(Constants.SCHEMA_DATA_SCHEMA);
    } catch (Exception e) {
      logger.error("Error occurred while processing camel exchange to get schema response - ", e);
      throw new RecapHarvesterException("Error while trying to get response - " + e.getMessage());
    }
  }

}
