package com.recap.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.camel.builder.PredicateBuilder.not;
import static org.apache.camel.builder.PredicateBuilder.and;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.apache.camel.model.dataformat.ZipFileDataFormat;
import org.apache.camel.component.aws.s3.S3Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.recap.config.BaseConfig;
import com.recap.config.EnvironmentConfig;
import com.recap.config.EnvironmentVariableNames;
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

    // When `ONLY_DO_UPDATES` is true, run the app in "Nightly Updates" mode
    // (i.e. fetch and process incremental updates and deletions)
    if (EnvironmentConfig.ONLY_DO_UPDATES) {

      // Establish base URI for S3 endpoints
      String baseS3Uri = "aws-s3://" + EnvironmentConfig.S3_BUCKET
          + "?accessKey=" + EnvironmentConfig.S3_ACCESS_KEY
          + "&secretKey=" + EnvironmentConfig.S3_SECRET_KEY;

      /**
       * Incremental Updates:
       *
       * Our overall strategy for incremental updates:
       * 1. Fetch /data-exports/NYPL/SCSBXml/Incremental/*.zip
       * 2. Move zips into processed folder
       * 3. For each zip, rename each enclosed xml file to an xml file with a random uuid filename
       * 4. Write updated zips to downloaded-updates/SCSBXML
       */

      String remotePath = EnvironmentConfig.S3_BASE_LOCATION + "/" + EnvironmentConfig.ACCESSION_DIRECTORY;

      from(baseS3Uri + "&prefix=" + remotePath)
        .choice()

        // When any file other than a .zip is found, just send to "processed":
        .when(
          not(header(S3Constants.KEY).endsWith(".zip"))
        )
          .to("direct:processedAccessions")
        .otherwise()
          .to("direct:processedAccessions")
          .split(new ZipSplitter())
            .streaming().process(new Processor() {

              @Override
              public void process(Exchange exchange) throws Exception {
                String fileName = (String) exchange.getIn().getHeader("CamelFileName");
                String zipFileName = (String) exchange.getIn().getHeader("CamelFileNameOnly");
                if (fileName.endsWith(".xml")) {
                  logger.info("Downloading contents of zipFile for accession -  " + zipFileName);
                  exchange.getIn().setHeader("CamelFileName", UUID.randomUUID() + ".xml");
                  logger.info("Renaming file name original - " + fileName + " to changed: "
                      + (String) exchange.getIn().getHeader("CamelFileName"));
                }
              }
            })
            .to("file:" + Constants.DOWNLOADED_UPDATES_ACCESSION_DIR)
            .end();

      // For each Incremental path processed, copy it to the .processed folder
      from("direct:processedAccessions").process(new Processor() {
          @Override
          public void process(Exchange exchange) throws RecapHarvesterException {
            // Set relevant headers, copyObject needs these set to work properly
            exchange.getIn().setHeader("CamelAwsS3BucketDestinationName", EnvironmentConfig.S3_BUCKET);

            // Build new ".processed" path:
            String originalKey = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
            String newKey = originalKey.replace(EnvironmentConfig.ACCESSION_DIRECTORY, EnvironmentConfig.ACCESSION_PROCESSED_DIRECTORY);
            exchange.getIn().setHeader("CamelAwsS3DestinationKey", newKey); 

            logger.info("Upload processed file: " + newKey);
          }
        })
          .to(baseS3Uri + "&operation=copyObject");

      // Loop over downloaded-updates/SCSBXML
      // .. Doing the same thing as above? (For each zip, renames enclosed xml to a random uuid xml filename?)
      // Writes xmls to downloaded-updates/SCSBXML
      from("file:" + Constants.DOWNLOADED_UPDATES_ACCESSION_DIR
          + "?maxMessagesPerPoll=1&delete=true&include=.*.zip").split(new ZipSplitter()).streaming()
              .process(new Processor() {

                @Override
                public void process(Exchange exchange) throws Exception {
                  String fileName = (String) exchange.getIn().getHeader("CamelFileName");
                  String zipFileName = (String) exchange.getIn().getHeader("CamelFileNameOnly");
                  if (fileName.endsWith(".xml")) {
                    logger.info("Downloading contents of zipFile for accession -  " + zipFileName);
                    exchange.getIn().setHeader("CamelFileName", UUID.randomUUID() + ".xml");
                    logger.info("Renaming file name original - " + fileName + " to changed: "
                        + (String) exchange.getIn().getHeader("CamelFileName"));
                  }
                }
              }).to("file:" + Constants.DOWNLOADED_UPDATES_ACCESSION_DIR).end();


      // For each xml in downloaded-updates/SCSBXML
      // For each bibRecord in the document, write to `direct:bib` and `direct:item`
      from("file:" + Constants.DOWNLOADED_UPDATES_ACCESSION_DIR
          + "?delete=true&maxMessagesPerPoll=1&eagerMaxMessagesPerPoll=false&recursive=true&include=.*.xml")
              .split(body().tokenizeXML("bibRecord", "")).streaming()
              .unmarshal("getBibRecordJaxbDataFormat").multicast().to("direct:bib", "direct:item");

      /**
       * Deaccessions
       *
       * Our general strategy for deaccessions:
       * 1. Fetch /share/recap/data-dump/uat/NYPL/Json/*.zip
       * 2. Rename all included jsons in each zip with random uuids
       * 3. Place updated zips in local directory: downloaded-updates/JSON
       */

      String deaccessionsRemotePath = EnvironmentConfig.S3_BASE_LOCATION + "/" + EnvironmentConfig.DEACCESSION_DIRECTORY;
      from(baseS3Uri + "&prefix=" + deaccessionsRemotePath + "&consumer.delay=60000")
        .choice()
        // When ends in .zip, download contents and send to "processed":
        .when(
          not(header(S3Constants.KEY).endsWith(".zip"))
        )
          .to("direct:processedDeaccessions")
        .otherwise()
          .to("direct:processedDeaccessions")
          .split(new ZipSplitter())
          .streaming().process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
              String fileName = (String) exchange.getIn().getHeader("CamelFileName");
              String zipFileName = (String) exchange.getIn().getHeader("CamelFileNameOnly");
              if (fileName.endsWith(".json")) {
                logger
                    .info("Downloading contents of zipFile for deaccession -  " + zipFileName);
                exchange.getIn().setHeader("CamelFileName", UUID.randomUUID() + ".json");
                logger.info("Renaming file name original - " + fileName + " to changed: "
                    + (String) exchange.getIn().getHeader("CamelFileName"));
              }
            }
          }).to("file:" + Constants.DOWNLOADED_UPDATES_DEACCESSION_DIR)
          .end();

      // For each Incremental path processed, copy it to the .processed folder
      from("direct:processedDeaccessions").process(new Processor() {
          @Override
          public void process(Exchange exchange) throws RecapHarvesterException {
            //set relevant headers, copyObject needs these set to work properly
            exchange.getIn().setHeader("CamelAwsS3BucketDestinationName", EnvironmentConfig.S3_BUCKET);

            // Build new ".processed" path:
            String originalKey = exchange.getIn().getHeader("CamelAwsS3Key", String.class);
            String newKey = originalKey.replace(EnvironmentConfig.DEACCESSION_DIRECTORY, EnvironmentConfig.DEACCESSION_PROCESSED_DIRECTORY);
            // String newKey = originalKey.substring(0, originalKey.length() - 1) + ".processed/";
            exchange.getIn().setHeader("CamelAwsS3DestinationKey", newKey); 

            logger.info("Upload processed deaccession file: " + newKey);
          }
        })
          .to(baseS3Uri + "&operation=copyObject");


      // Loop over downloaded-updates/JSON
      // Appears to repeat work above of renaming the files in each zip with random uuids
      // Assume it's writing the jsons to downloaded-updates/JSON
      from("file:" + Constants.DOWNLOADED_UPDATES_DEACCESSION_DIR
          + "?maxMessagesPerPoll=1&delete=true&include=.*.zip").split(new ZipSplitter()).streaming()
              .process(new Processor() {

                @Override
                public void process(Exchange exchange) throws Exception {
                  String fileName = (String) exchange.getIn().getHeader("CamelFileName");
                  String zipFileName = (String) exchange.getIn().getHeader("CamelFileNameOnly");
                  if (fileName.endsWith(".json")) {
                    logger
                        .info("Downloading contents of zipFile for deaccession -  " + zipFileName);
                    exchange.getIn().setHeader("CamelFileName", UUID.randomUUID() + ".json");
                    logger.info("Renaming file name original - " + fileName + " to changed: "
                        + (String) exchange.getIn().getHeader("CamelFileName"));
                  }
                }
              }).to("file:" + Constants.DOWNLOADED_UPDATES_DEACCESSION_DIR).end();

      from("scheduler://deletionFilePoller?delay=60000").process(new Processor() {

        @Override
        public void process(Exchange exchange) throws RecapHarvesterException {
          File scsbXmlFilesLocalDir = new File(Constants.DOWNLOADED_UPDATES_ACCESSION_DIR);
          scsbXmlFilesLocalDir.mkdirs();
          boolean updatesAreDone = true;
          for (File file : scsbXmlFilesLocalDir.listFiles()) {
            if (file.getName().trim().endsWith(".zip") || file.getName().trim().endsWith(".xml")) {
              updatesAreDone = false;
              break;
            }
          }
          if (updatesAreDone) {
            File delInfoJsonFileLocalDir = new File(Constants.DOWNLOADED_UPDATES_DEACCESSION_DIR);
            delInfoJsonFileLocalDir.mkdirs();
            File[] files = delInfoJsonFileLocalDir.listFiles();
            if (files.length > 0) {
              for (File file : delInfoJsonFileLocalDir.listFiles()) {
                if (file.getName().trim().endsWith(".json")) {
                  exchange.getIn().setBody(delInfoJsonFileLocalDir, File.class);
                  break;
                }
              }
            }
          }
        }
      }).process(new DeleteInfoProcessor(true)).multicast().to("direct:deletedBibsProcess",
          "direct:deletedItemsProcess");

    } else {
      String scsbexportstaging =
          System.getenv(EnvironmentVariableNames.SCSB_EXPORT_STAGING_LOCATION);
      from("file:" + scsbexportstaging + "?delete=true&maxMessagesPerPoll=1")
          .split(body().tokenizeXML("bibRecord", "")).streaming()
          .unmarshal("getBibRecordJaxbDataFormat").multicast().to("direct:bib", "direct:item");
    }



    from("direct:bib").process(new BibProcessor(baseConfig)).process(new Processor() {

      @Override
      public void process(Exchange exchange) throws Exception {
        Bib bib = exchange.getIn().getBody(Bib.class);
        List<Bib> bibs = new ArrayList<>();
        bibs.add(bib);
        exchange.getIn().setBody(bibs);
      }
    }).process(new BibsAvroProcessor(schema, retryTemplate, producerTemplate))
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
