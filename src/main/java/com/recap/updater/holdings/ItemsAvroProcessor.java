package com.recap.updater.holdings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.recap.updater.utils.NYPLSchema;
import com.recap.updater.utils.SchemaUtils;

public class ItemsAvroProcessor implements Processor {

  private static Logger logger = LoggerFactory.getLogger(ItemsAvroProcessor.class);

  private String schemaJson;

  public ItemsAvroProcessor(NYPLSchema schema, RetryTemplate retryTemplate,
      ProducerTemplate producerTemplate) throws RecapHarvesterException {
    if (schema.getItemSchemaJson() == null)
      schema.setItemSchemaJson(new SchemaUtils().getSchema(retryTemplate, producerTemplate,
          EnvironmentConfig.ITEM_SCHEMA_API));
    this.schemaJson = schema.getItemSchemaJson();
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException, IOException {
    try {
      List<Item> items = exchange.getIn().getBody(List.class);
      List<byte[]> avroItems = new ArrayList<>();
      Schema schema = new Schema.Parser().setValidate(true).parse(schemaJson);
      AvroSchema avroSchema = new AvroSchema(schema);
      AvroMapper avroMapper = new AvroMapper();
      for (Item item : items) {
        System.out.println(new ObjectMapper().writeValueAsString(item));
        byte[] avroItem = avroMapper.writer(avroSchema).writeValueAsBytes(item);
        avroItems.add(avroItem);
      }
      exchange.getIn().setBody(avroItems);
    } catch (JsonProcessingException jsonProcessingException) {
      logger.error("Error occurred while doing avro processing for item - ",
          jsonProcessingException);
      throw new RecapHarvesterException("Error occurred while doing avro processing for item - "
          + jsonProcessingException.getMessage());
    }
  }
}
