package com.recap.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.ZipFileDataFormat;
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
import com.recap.stream.KinesisProcessor;
import com.recap.updater.bib.BibsAvroProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.deletions.DeleteInfoProcessor;
import com.recap.updater.deletions.DeletedBibsProcessor;
import com.recap.updater.deletions.DeletedItemsProcessor;
import com.recap.updater.holdings.HoldingListProcessor;
import com.recap.updater.holdings.ItemsAvroProcessor;
import com.recap.updater.holdings.ItemsProcessor;
import com.recap.updater.utils.NYPLSchema;
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

  @Autowired
  private NYPLSchema schema;

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

    if (EnvironmentConfig.ONLY_DO_UPDATES) {
      ZipFileDataFormat zipFile = new ZipFileDataFormat();
      zipFile.setUsingIterator(true);

      from("sftp://" + EnvironmentConfig.FTP_HOST + ":" + EnvironmentConfig.FTP_PORT
          + EnvironmentConfig.FTP_BASE_LOCATION + "/" + EnvironmentConfig.ACCESSION_DIRECTORY
          + "?privateKeyFile=" + EnvironmentConfig.FTP_PRIVATE_KEY_FILE_LOCATION + "&include=.*.zip"
          + "&consumer.delay=60000&streamDownload=true&move="
          + EnvironmentConfig.FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY + "&moveFailed="
          + EnvironmentConfig.FTP_COMPRESSED_FILES_ERROR_DIRECTORY).startupOrder(1).streamCaching()
              .unmarshal(zipFile).split(bodyAs(Iterator.class)).streaming()
              .to("file:" + EnvironmentConfig.UNCOMPRESSED_FILES_DIRECTORY + "/"
                  + EnvironmentConfig.ACCESSION_DIRECTORY);



      from("file:" + EnvironmentConfig.UNCOMPRESSED_FILES_DIRECTORY + "/"
          + EnvironmentConfig.ACCESSION_DIRECTORY
          + "?delete=true&maxMessagesPerPoll=1&eagerMaxMessagesPerPoll=false&recursive=true&include=.*.xml")
              .startupOrder(2).split(body().tokenizeXML("bibRecord", "")).streaming()
              .unmarshal("getBibRecordJaxbDataFormat").multicast().to("direct:bib", "direct:item");



      from("sftp://" + EnvironmentConfig.FTP_HOST + ":" + EnvironmentConfig.FTP_PORT
          + EnvironmentConfig.FTP_BASE_LOCATION + "/" + EnvironmentConfig.DEACCESSION_DIRECTORY
          + "?privateKeyFile=" + EnvironmentConfig.FTP_PRIVATE_KEY_FILE_LOCATION + "&include=.*.zip"
          + "&consumer.delay=60000&streamDownload=true&move="
          + EnvironmentConfig.FTP_COMPRESSED_FILES_PROCESSED_DIRECTORY + "&moveFailed="
          + EnvironmentConfig.FTP_COMPRESSED_FILES_ERROR_DIRECTORY).startupOrder(3).streamCaching()
              .unmarshal(zipFile).split(bodyAs(Iterator.class)).streaming()
              .to("file:" + EnvironmentConfig.UNCOMPRESSED_FILES_DIRECTORY + "/"
                  + EnvironmentConfig.DEACCESSION_DIRECTORY);


      from("timer://startDeleteProcess?fixedRate=true&period=60000").startupOrder(4)
          .process(new Processor() {

            @Override
            public void process(Exchange exchange) throws RecapHarvesterException {
              File scsbXmlFilesLocalDir = new File(EnvironmentConfig.UNCOMPRESSED_FILES_DIRECTORY
                  + "/" + EnvironmentConfig.ACCESSION_DIRECTORY);
              boolean updatesAreDone = true;
              for (File file : scsbXmlFilesLocalDir.listFiles()) {
                if (file.getName().trim().endsWith(".xml")) {
                  updatesAreDone = false;
                  break;
                }
              }
              if (updatesAreDone) {
                File delInfoJsonFileLocalDir =
                    new File(EnvironmentConfig.UNCOMPRESSED_FILES_DIRECTORY + "/"
                        + EnvironmentConfig.DEACCESSION_DIRECTORY);
                File[] files = delInfoJsonFileLocalDir.listFiles();
                if(files.length > 0){
                  for (File file : delInfoJsonFileLocalDir.listFiles()) {
                    if (file.getName().trim().endsWith(".json")) {
                      exchange.getIn().setBody(delInfoJsonFileLocalDir, File.class);
                      break;
                    }
                  }
                }
              }
            }
          })
          .process(new DeleteInfoProcessor(true))
          .multicast().to("direct:deletedBibsProcess", "direct:deletedItemsProcess");
    } else {
      from("file:" + scsbexportstaging + "?delete=true&maxMessagesPerPoll=1")
          .split(body().tokenizeXML("bibRecord", "")).streaming()
          .unmarshal("getBibRecordJaxbDataFormat").multicast().to("direct:bib", "direct:item");
    }
    
    

    from("direct:bib").process(new BibProcessor(baseConfig))
        .process(new Processor() {
          
          @Override
          public void process(Exchange exchange) throws Exception {
            Bib bib = exchange.getIn().getBody(Bib.class);
            List<Bib> bibs = new ArrayList<>();
            bibs.add(bib);
            exchange.getIn().setBody(bibs);
          }
        })
        .process(new BibsAvroProcessor(schema, retryTemplate, producerTemplate))
        .process(new KinesisProcessor(baseConfig, EnvironmentConfig.KINESIS_BIB_STREAM));
    
    

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
        .process(new ItemsAvroProcessor(schema, retryTemplate, producerTemplate))
        .process(new KinesisProcessor(baseConfig, EnvironmentConfig.KINESIS_ITEM_STREAM));
    
    
    
    from("direct:deletedBibsProcess").process(new DeletedBibsProcessor())
    .process(new BibsAvroProcessor(schema, retryTemplate, producerTemplate))
    .process(new KinesisProcessor(baseConfig, EnvironmentConfig.KINESIS_BIB_STREAM));
    
    from("direct:deletedItemsProcess").process(new DeletedItemsProcessor())
    .process(new ItemsAvroProcessor(schema, retryTemplate, producerTemplate))
    .process(new KinesisProcessor(baseConfig, EnvironmentConfig.KINESIS_ITEM_STREAM));
  }

}
