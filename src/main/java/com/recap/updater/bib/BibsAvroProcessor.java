package com.recap.updater.bib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.event.CamelContextStartedEvent;
import org.apache.camel.support.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.recap.config.EnvironmentConfig;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Bib;
import com.recap.updater.utils.NYPLSchema;
import com.recap.updater.utils.SchemaUtils;
import org.springframework.stereotype.Component;

import static com.recap.config.EnvironmentConfig.BIB_SCHEMA_API;

@Component
public class BibsAvroProcessor implements Processor {

  private NYPLSchema schema;
  private RetryTemplate retryTemplate;
  private ProducerTemplate producerTemplate;

  private static Logger logger = LoggerFactory.getLogger(BibsAvroProcessor.class);

  public BibsAvroProcessor(NYPLSchema schema, RetryTemplate retryTemplate,
    ProducerTemplate producerTemplate) {
    this.schema = schema;
    this.retryTemplate = retryTemplate;
    this.producerTemplate = producerTemplate;
  }

  @EventListener({CamelContextStartedEvent.class, ApplicationReadyEvent.class})
  public void initializeSchema() throws RecapHarvesterException {
    if (schema.getBibSchemaJson() == null) {
      schema.setBibSchemaJson(new SchemaUtils().getSchema(retryTemplate, producerTemplate, EnvironmentConfig.BIB_SCHEMA_API));
    }
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException, IOException {
    try {
      Object body = exchange.getIn().getBody();
      if (body != null && body.getClass() != DefaultMessage.class) {
        List<Bib> bibs = exchange.getIn().getBody(List.class);
        List<byte[]> avroBibs = new ArrayList<>();
        String bibIds = "";
        for (Bib bib : bibs) {
          bibIds += bib.getId() + ", ";
          Schema schema = new Schema.Parser().setValidate(true).parse(this.schema.getBibSchemaJson());
          AvroSchema avroSchema = new AvroSchema(schema);
          AvroMapper avroMapper = new AvroMapper();
          byte[] avroBib = avroMapper.writer(avroSchema).writeValueAsBytes(bib);
          avroBibs.add(avroBib);
        }
        logger.info("Avro processed for bibs with ids: " + bibIds);
        exchange.getIn().setBody(avroBibs);
      }
    } catch (JsonProcessingException jsonProcessingException) {
      logger.error("Error occurred while doing avro processing for bib - ",
          jsonProcessingException);
      throw new RecapHarvesterException("Error occurred while doing avro processing for bib - "
          + jsonProcessingException.getMessage());
    }
  }

}
