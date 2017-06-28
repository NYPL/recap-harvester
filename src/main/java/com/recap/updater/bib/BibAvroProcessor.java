package com.recap.updater.bib;

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
import com.recap.models.Bib;
import com.recap.updater.utils.NYPLSchema;
import com.recap.updater.utils.SchemaUtils;


public class BibAvroProcessor implements Processor {

  private String schemaJson;

  private static Logger logger = LoggerFactory.getLogger(BibAvroProcessor.class);

  public BibAvroProcessor(NYPLSchema schema, RetryTemplate retryTemplate,
      ProducerTemplate producerTemplate) throws RecapHarvesterException {
    if (schema.getBibSchemaJson() == null)
      schema.setBibSchemaJson(new SchemaUtils().getSchema(retryTemplate, producerTemplate,
          EnvironmentConfig.BIB_SCHEMA_API));
    schemaJson = schema.getBibSchemaJson();
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException, IOException {
    try {
      Bib bib = exchange.getIn().getBody(Bib.class);
      logger.info(new ObjectMapper().writeValueAsString(bib));
      Schema schema = new Schema.Parser().setValidate(true).parse(schemaJson);
      AvroSchema avroSchema = new AvroSchema(schema);
      AvroMapper avroMapper = new AvroMapper();
      byte[] avroBib = avroMapper.writer(avroSchema).writeValueAsBytes(bib);
      exchange.getIn().setBody(avroBib);
    } catch (JsonProcessingException jsonProcessingException) {
      logger.error("Error occurred while doing avro processing for bib - ",
          jsonProcessingException);
      throw new RecapHarvesterException("Error occurred while doing avro processing for bib - "
          + jsonProcessingException.getMessage());
    }
  }

}
