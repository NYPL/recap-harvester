package com.recap.stream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.kinesis.model.PutRecordsResultEntry;
import com.google.common.collect.Lists;
import com.recap.config.BaseConfig;
import com.recap.config.EnvironmentConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;

public class KinesisProcessor implements Processor {

  private BaseConfig baseConfig;

  private String streamName;

  private static Logger logger = LoggerFactory.getLogger(KinesisProcessor.class);

  public KinesisProcessor(BaseConfig baseConfig, String streamName) {
    this.baseConfig = baseConfig;
    this.streamName = streamName;
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
    try {
      Object body = exchange.getIn().getBody();
      if (body != null && body.getClass() != DefaultMessage.class) {
        List<byte[]> avroRecords = exchange.getIn().getBody(List.class);
        List<List<byte[]>> listOfSplitRecords =
            Lists.partition(avroRecords, Constants.KINESIS_PUT_RECORDS_MAX_SIZE);
        for (List<byte[]> splitAvroRecords : listOfSplitRecords) {
          sendToKinesis(splitAvroRecords);
        }
      }
    } catch (Exception e) {
      logger.error("Error occurred while sending records to kinesis - ", e);
      throw new RecapHarvesterException(
          "Error occurred while sending records to kinesis - " + e.getMessage());
    }
  }

  public void sendToKinesis(List<byte[]> avroRecords) throws RecapHarvesterException {
    try {
      if (!(avroRecords.size() > 0))
        return;
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
      PutRecordsRequest putRecordsRequest =
          createPutRecordsRequest(listPutRecordsRequestEntry, avroRecords);
      PutRecordsResult putRecordsResult =
          getPutRecordsResultAfterPostingToKinesis(putRecordsRequest);

      validateResponse(putRecordsResult, listPutRecordsRequestEntry, putRecordsRequest);
    } catch (Exception e) {
      logger.error("Error occurred while sending records to kinesis - ", e);
      throw new RecapHarvesterException(
          "Error occurred while sending records to kinesis - " + e.getMessage());
    }
  }

  public PutRecordsRequest createPutRecordsRequest(
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry, List<byte[]> avroRecords)
      throws RecapHarvesterException {
    try {
      PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
      putRecordsRequest.setStreamName(streamName);
      for (byte[] avroRecord : avroRecords) {
        PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
        putRecordsRequestEntry.setData(ByteBuffer.wrap(avroRecord));
        putRecordsRequestEntry.setPartitionKey(Long.toString(System.currentTimeMillis()));
        listPutRecordsRequestEntry.add(putRecordsRequestEntry);
      }
      putRecordsRequest.setRecords(listPutRecordsRequestEntry);
      return putRecordsRequest;
    } catch (Exception e) {
      logger.error("Error occurred while creating PutRecordsRequest - " + e.getMessage());
      throw new RecapHarvesterException(
          "Returning error while trying to prepare PutRecordsRequest - " + e.getMessage());
    }
  }

  public PutRecordsResult getPutRecordsResultAfterPostingToKinesis(
      PutRecordsRequest putRecordsRequest) throws RecapHarvesterException {
    try {
      return baseConfig.getAmazonKinesisClient().putRecords(putRecordsRequest);
    } catch (Exception e) {
      logger.error(
          "Error occurred on sending records to kinesis and retrieving PutRecordsResult - ", e);
      throw new RecapHarvesterException(
          "Error occurred on sending records to kinesis and retrieving PutRecordsResult "
              + e.getMessage());
    }
  }

  public void validateResponse(PutRecordsResult putRecordsResult,
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry, PutRecordsRequest putRecordsRequest)
      throws RecapHarvesterException {
    try {
      if (!(putRecordsResult.getFailedRecordCount() > 0))
        return;
      else {
        logger.info("Got some records didn't make it into kinesis - "
            + putRecordsResult.getFailedRecordCount() + " - is the failed record count");
        resendFailedRecordsToKinesis(putRecordsResult, listPutRecordsRequestEntry,
            putRecordsRequest);
      }
    } catch (Exception e) {
      logger.error(
          "Error occurred while validating kinesis response after posting results to kinesis - ",
          e);
      throw new RecapHarvesterException(
          "Error occurred while validating kinesis response after posting results to kinesis - "
              + e.getMessage());
    }
  }

  public void resendFailedRecordsToKinesis(PutRecordsResult putRecordsResult,
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry, PutRecordsRequest putRecordsRequest)
      throws RecapHarvesterException {
    try {
      final List<PutRecordsRequestEntry> failedRecordsList = new ArrayList<>();
      final List<PutRecordsResultEntry> listPutRecordsResultEntry = putRecordsResult.getRecords();
      for (int i = 0; i < listPutRecordsResultEntry.size(); i++) {
        final PutRecordsRequestEntry putRecordsRequestEntry = listPutRecordsRequestEntry.get(i);
        final PutRecordsResultEntry putRecordsResultEntry = listPutRecordsResultEntry.get(i);
        if (putRecordsResultEntry.getErrorCode() != null) {
          failedRecordsList.add(putRecordsRequestEntry);
        }
      }
      listPutRecordsRequestEntry = failedRecordsList;
      putRecordsRequest.setRecords(listPutRecordsRequestEntry);
      putRecordsResult = getPutRecordsResultAfterPostingToKinesis(putRecordsRequest);
      validateResponse(putRecordsResult, listPutRecordsRequestEntry, putRecordsRequest);
    } catch (Exception e) {
      logger.error("Error occurred while resending records to kinesis - ", e);
      throw new RecapHarvesterException(
          "Error occurred while resending records to kinesis - " + e.getMessage());
    }
  }

}
