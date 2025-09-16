package com.recap.stream;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResultEntry;

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
      PutRecordsResponse putRecordsResult =
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
      PutRecordsRequest.Builder putRecordsRequest = PutRecordsRequest.builder();
      putRecordsRequest.streamName(streamName);
      for (byte[] avroRecord : avroRecords) {
        PutRecordsRequestEntry.Builder putRecordsRequestEntry = PutRecordsRequestEntry.builder();
        putRecordsRequestEntry.data(SdkBytes.fromByteArray(avroRecord));
        putRecordsRequestEntry.partitionKey(Long.toString(System.currentTimeMillis()));
        listPutRecordsRequestEntry.add(putRecordsRequestEntry.build());
      }
      putRecordsRequest.records(listPutRecordsRequestEntry);
      return putRecordsRequest.build();
    } catch (Exception e) {
      logger.error("Error occurred while creating PutRecordsRequest - " + e.getMessage());
      throw new RecapHarvesterException(
          "Returning error while trying to prepare PutRecordsRequest - " + e.getMessage());
    }
  }

  public PutRecordsResponse getPutRecordsResultAfterPostingToKinesis(
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

  public void validateResponse(PutRecordsResponse putRecordsResult,
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry, PutRecordsRequest putRecordsRequest)
      throws RecapHarvesterException {
    try {
      if (!(putRecordsResult.failedRecordCount() > 0))
        return;
      else {
        logger.info("Got some records didn't make it into kinesis - "
            + putRecordsResult.failedRecordCount() + " - is the failed record count");
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

  public void resendFailedRecordsToKinesis(PutRecordsResponse putRecordsResult,
      List<PutRecordsRequestEntry> listPutRecordsRequestEntry, PutRecordsRequest putRecordsRequest)
      throws RecapHarvesterException {
    try {
      final List<PutRecordsRequestEntry> failedRecordsList = new ArrayList<>();
      final List<PutRecordsResultEntry> listPutRecordsResultEntry = putRecordsResult.records();
      for (int i = 0; i < listPutRecordsResultEntry.size(); i++) {
        final PutRecordsRequestEntry putRecordsRequestEntry = listPutRecordsRequestEntry.get(i);
        final PutRecordsResultEntry putRecordsResultEntry = listPutRecordsResultEntry.get(i);
        if (putRecordsResultEntry.errorCode() != null) {
          failedRecordsList.add(putRecordsRequestEntry);
        }
      }
      listPutRecordsRequestEntry = failedRecordsList;
      putRecordsRequest = putRecordsRequest.toBuilder().records(listPutRecordsRequestEntry).build();
      putRecordsResult = getPutRecordsResultAfterPostingToKinesis(putRecordsRequest);
      validateResponse(putRecordsResult, listPutRecordsRequestEntry, putRecordsRequest);
    } catch (Exception e) {
      logger.error("Error occurred while resending records to kinesis - ", e);
      throw new RecapHarvesterException(
          "Error occurred while resending records to kinesis - " + e.getMessage());
    }
  }

}
