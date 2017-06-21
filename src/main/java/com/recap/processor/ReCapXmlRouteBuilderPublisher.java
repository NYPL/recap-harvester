package com.recap.processor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.config.EnvironmentConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Bib;
import com.recap.updater.bib.BibAvroProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsAvroProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.xml.models.BibRecord;

@Component
public class ReCapXmlRouteBuilderPublisher extends RouteBuilder {

  @Value("${scsbexportstagingLocation}")
  private String scsbexportstaging;

  @Autowired
  private BaseConfig baseConfig;

  @Autowired
  private RetryTemplate retryTemplate;

  @Autowired
  private ProducerTemplate producerTemplate;

  private static Logger logger = LoggerFactory.getLogger(ReCapXmlRouteBuilderPublisher.class);

  @Override
  public void configure() throws Exception {
    onException(RecapHarvesterException.class).process(new Processor() {

      @Override
      public void process(Exchange exchange) throws RecapHarvesterException {
        Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        logger.error("RECAPHARVESTER ERROR HANDLED - ", caught);
      }
    }).handled(true);

    onException(Exception.class).process(new Processor() {

      @Override
      public void process(Exchange exchange) throws Exception {
        Throwable caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        logger.error("APP FATAL UNEXPECTED ERROR - ", caught);
      }
    }).handled(true);

    from("file:" + scsbexportstaging
        + "?maxMessagesPerPoll=1").split(body().tokenizeXML("bibRecord", "")).streaming()
            .unmarshal("getBibRecordJaxbDataFormat").multicast().to("direct:bib", "direct:item");

    from("direct:bib").process(new BibProcessor(baseConfig))
        .process(new BibAvroProcessor(producerTemplate, retryTemplate)).process(new Processor() {

          @Override
          public void process(Exchange exchange) throws Exception {
            byte[] body = (byte[]) exchange.getIn().getBody();
            ByteBuffer byteBuffer = ByteBuffer.wrap(body);
            exchange.getIn().setBody(byteBuffer);
            exchange.getIn().setHeader(Constants.PARTITION_KEY, System.currentTimeMillis());
            exchange.getIn().setHeader(Constants.SEQUENCE_NUMBER, System.currentTimeMillis());
          }
        }).to("aws-kinesis://" + EnvironmentConfig.KINESIS_BIB_STREAM
            + "?amazonKinesisClient=#getAmazonKinesisClient");

    from("direct:item").process(new Processor() {

      @Override
      public void process(Exchange exchange) throws RecapHarvesterException {
        Map<String, Object> exchangeContents = new HashMap<>();
        BibRecord bibRecord = (BibRecord) exchange.getIn().getBody();
        exchangeContents.put(Constants.BIB_RECORD, bibRecord);
        exchange.getIn().setBody(exchangeContents);
      }
    }).process(new HoldingListProcessor()).process(new Processor() {

      @Override
      public void process(Exchange exchange) throws RecapHarvesterException {
        Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
        Bib bib = new BibProcessor(baseConfig)
            .getBibFromBibRecord((BibRecord) exchangeContents.get(Constants.BIB_RECORD));
        exchangeContents.put(Constants.BIB, bib);
        exchange.getIn().setBody(exchangeContents);
      }
    }).process(new ItemsProcessor())
        .split(body()).process(new ItemsAvroProcessor(retryTemplate, producerTemplate))
        .process(new Processor() {

          @Override
          public void process(Exchange exchange) throws Exception {
            byte[] body = (byte[]) exchange.getIn().getBody();
            ByteBuffer byteBuffer = ByteBuffer.wrap(body);
            exchange.getIn().setBody(byteBuffer);
            exchange.getIn().setHeader(Constants.PARTITION_KEY, System.currentTimeMillis());
            exchange.getIn().setHeader(Constants.SEQUENCE_NUMBER, System.currentTimeMillis());
          }
        }).to("aws-kinesis://" + EnvironmentConfig.KINESIS_ITEM_STREAM
            + "?amazonKinesisClient=#getAmazonKinesisClient");
  }

}
