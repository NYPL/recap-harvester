package com.recap.updater.holdings;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.kinesis.model.PutRecordsResultEntry;
import com.recap.config.BaseConfig;
import com.recap.config.EnvironmentConfig;
import com.recap.exceptions.RecapHarvesterException;

public class KinesisProcessor implements Processor {

  private BaseConfig baseConfig;

  private EnvironmentConfig envConfig;

  private static Logger logger = LoggerFactory.getLogger(KinesisProcessor.class);

  public KinesisProcessor(BaseConfig baseConfig) {
    this.baseConfig = baseConfig;
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
    try {
      List<byte[]> avroItems = exchange.getIn().getBody(List.class);
      PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
      putRecordsRequest.setStreamName(EnvironmentConfig.KINESIS_ITEM_STREAM);
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
      for (byte[] avroItem : avroItems) {
        PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
        putRecordsRequestEntry.setData(ByteBuffer.wrap(avroItem));
        putRecordsRequestEntry.setPartitionKey(Long.toString(System.currentTimeMillis()));
        listPutRecordsRequestEntry.add(putRecordsRequestEntry);
      }
      putRecordsRequest.setRecords(listPutRecordsRequestEntry);
      PutRecordsResult putRecordsResult =
          baseConfig.getAmazonKinesisClient().putRecords(putRecordsRequest);
      while (putRecordsResult.getFailedRecordCount() > 0) {
        final List<PutRecordsRequestEntry> failedRecordsList = new ArrayList<>();
        final List<PutRecordsResultEntry> listPutRecordsResultEntry = putRecordsResult.getRecords();
        for (int i = 0; i < listPutRecordsResultEntry.size(); i++) {
          final PutRecordsRequestEntry putRecordsRequestEntry = listPutRecordsRequestEntry.get(i);
          final PutRecordsResultEntry putRecordsResultEntry = listPutRecordsResultEntry.get(i);
          if (putRecordsResultEntry.getErrorCode() != null) {
            failedRecordsList.add(putRecordsRequestEntry);
          }
          listPutRecordsRequestEntry = failedRecordsList;
          putRecordsRequest.setRecords(listPutRecordsRequestEntry);
          putRecordsResult = baseConfig.getAmazonKinesisClient().putRecords(putRecordsRequest);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurred while sending items to kinesis - ", e);
      throw new RecapHarvesterException(
          "Error occurred while sending records to kinesis - " + e.getMessage());
    }
  }

}