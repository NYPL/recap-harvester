package com.recap.updater.holdings;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.recap.config.EnvironmentConfig;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Item;
import com.recap.updater.utils.SchemaUtils;

public class ItemsAvroProcessor implements Processor {

  private RetryTemplate retryTemplate;

  private ProducerTemplate producerTemplate;

  private static Logger logger = LoggerFactory.getLogger(ItemsAvroProcessor.class);

  public ItemsAvroProcessor(RetryTemplate retryTemplate, ProducerTemplate producerTemplate) {
    this.retryTemplate = retryTemplate;
    this.producerTemplate = producerTemplate;
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException, IOException {
    try {
      Item item = exchange.getIn().getBody(Item.class);
      logger.info("Item - " + new ObjectMapper().writeValueAsString(item));
      String schemaJson = new SchemaUtils().getSchema(retryTemplate, producerTemplate,
          EnvironmentConfig.ITEM_SCHEMA_API);
      Schema schema = new Schema.Parser().setValidate(true).parse(schemaJson);
      AvroSchema avroSchema = new AvroSchema(schema);
      AvroMapper avroMapper = new AvroMapper();
      byte[] avroItem = avroMapper.writer(avroSchema).writeValueAsBytes(item);
      exchange.getIn().setBody(avroItem);
    } catch (JsonProcessingException jsonProcessingException) {
      logger.error("Error occurred while doing avro processing for item - ",
          jsonProcessingException);
      throw new RecapHarvesterException("Error occurred while doing avro processing for item - "
          + jsonProcessingException.getMessage());
    }
  }
}
