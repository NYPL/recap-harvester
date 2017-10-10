package com.recap.updater.holdings;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.test.junit4.ExchangeTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.kinesis.model.PutRecordsResultEntry;
import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.stream.KinesisProcessor;

public class KinesisProcessorTest extends ExchangeTestSupport {


  private List<byte[]> getListOfItems(int numOfItems) {
    List<byte[]> items = new ArrayList<>();
    for (int i = 0; i < numOfItems; i++) {
      items.add(new byte[i]);
    }
    return items;
  }

  @Test
  public void testBelowKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(10);
    KinesisProcessor kinesisProcessor =
        Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
    doNothing().when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(1)).sendToKinesis(anyList());
  }

  @Test
  public void testEqualToKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(Constants.KINESIS_PUT_RECORDS_MAX_SIZE);
    KinesisProcessor kinesisProcessor =
        Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
    doNothing().when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(1)).sendToKinesis(anyList());
  }

  @Test
  public void testAboveKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(510);
    KinesisProcessor kinesisProcessor =
        Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
    doNothing().when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(2)).sendToKinesis(anyList());
  }

  @Test
  public void testMoreThanDoubleOfKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(1050);
    KinesisProcessor kinesisProcessor =
        Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
    doNothing().when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(3)).sendToKinesis(anyList());
  }

  @Test
  public void testSendToKinesisWhenAllRecordsAreSentSuccessfullyAtOnce()
      throws RecapHarvesterException {
    KinesisProcessor kinesisProcessor = new KinesisProcessor(null, "mockStreamName");
    KinesisProcessor spyKinesisProcessor = Mockito.spy(kinesisProcessor);
    List<byte[]> mockAvroRecords = new ArrayList<>();
    int maxRecords = 10;
    for (int i = 1; i <= maxRecords; i++) {
      mockAvroRecords.add(new String("avroRecord" + i).getBytes());
    }
    PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
    List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
    doReturn(putRecordsRequest).when(spyKinesisProcessor)
        .createPutRecordsRequest(listPutRecordsRequestEntry, mockAvroRecords);
    PutRecordsResult putRecordsResult = new PutRecordsResult();
    putRecordsResult.setFailedRecordCount(0);
    doReturn(putRecordsResult).when(spyKinesisProcessor)
        .getPutRecordsResultAfterPostingToKinesis(putRecordsRequest);
    spyKinesisProcessor.sendToKinesis(mockAvroRecords);
  }

  @Test
  public void testCreatePutRecordsRequest() throws RecapHarvesterException {
    KinesisProcessor kinesisProcessor = new KinesisProcessor(null, "mockStreamName");
    List<byte[]> mockAvroRecords = new ArrayList<>();
    int maxRecords = 10;
    for (int i = 1; i <= maxRecords; i++) {
      mockAvroRecords.add(new String("avroRecord" + i).getBytes());
    }
    List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
    PutRecordsRequest putRecordsRequest =
        kinesisProcessor.createPutRecordsRequest(listPutRecordsRequestEntry, mockAvroRecords);
    List<PutRecordsRequestEntry> recordsOfPutRecordsReq = putRecordsRequest.getRecords();
    Assert.assertTrue(recordsOfPutRecordsReq.size() == maxRecords ? true : false);
    PutRecordsRequestEntry putRecordsReqEntry = recordsOfPutRecordsReq.get(5);
    Assert.assertEquals("avroRecord6", new String(putRecordsReqEntry.getData().array()));
    Assert.assertTrue(putRecordsReqEntry.getPartitionKey().length() > 0);
  }

  @Test
  public void testValidateResponse() throws RecapHarvesterException {
    KinesisProcessor kinesisProcessor = new KinesisProcessor(null, null);
    KinesisProcessor spyKinesisProcessor = Mockito.spy(kinesisProcessor);
    PutRecordsResult putRecordsResult = new PutRecordsResult();
    putRecordsResult.setFailedRecordCount(100);
    List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
    PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
    doNothing().when(spyKinesisProcessor).resendFailedRecordsToKinesis(putRecordsResult,
        listPutRecordsRequestEntry, putRecordsRequest);
    spyKinesisProcessor.validateResponse(putRecordsResult, listPutRecordsRequestEntry,
        putRecordsRequest);
  }

  @Test
  public void testResendFailedRecordsToKinesis() throws RecapHarvesterException {
    KinesisProcessor kinesisProcessor = new KinesisProcessor(null, null);
    KinesisProcessor spyKinesisProcessor = Mockito.spy(kinesisProcessor);
    List<byte[]> mockAvroRecords = new ArrayList<>();
    int maxRecords = 3;
    for (int i = 1; i <= maxRecords; i++) {
      mockAvroRecords.add(new String("avroRecord" + i).getBytes());
    }
    List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
    PutRecordsRequest putRecordsRequest =
        spyKinesisProcessor.createPutRecordsRequest(listPutRecordsRequestEntry, mockAvroRecords);
    listPutRecordsRequestEntry = putRecordsRequest.getRecords();
    List<PutRecordsResultEntry> listPutRecordsResultEntry = new ArrayList<>();
    for (int i = 0; i < maxRecords; i++) {
      if (i == 1) {
        PutRecordsResultEntry putRecordsResultEntry = new PutRecordsResultEntry();
        putRecordsResultEntry.setErrorCode("mockErrorCode");
        listPutRecordsResultEntry.add(putRecordsResultEntry);
      } else {
        PutRecordsResultEntry putRecordsResultEntry = new PutRecordsResultEntry();
        putRecordsResultEntry.setSequenceNumber("mockSequenceNumber-" + i);
        listPutRecordsResultEntry.add(putRecordsResultEntry);
      }
    }
    PutRecordsResult putRecordsResult = new PutRecordsResult();
    putRecordsResult.setFailedRecordCount(1);
    putRecordsResult.setRecords(listPutRecordsResultEntry);


    PutRecordsResultEntry putRecordsResultEntryFailed = new PutRecordsResultEntry();
    putRecordsResultEntryFailed.setErrorCode("mockErrorCode");
    List<PutRecordsResultEntry> listFailedPutRecordsResultEntry = new ArrayList<>();
    listFailedPutRecordsResultEntry.add(putRecordsResultEntryFailed);
    PutRecordsResult putRecordsResultFailed = new PutRecordsResult();
    putRecordsResultFailed.setFailedRecordCount(1);
    putRecordsResultFailed.setRecords(listFailedPutRecordsResultEntry);

    PutRecordsResultEntry putRecordsResultEntrySuccess = new PutRecordsResultEntry();
    putRecordsResultEntrySuccess.setSequenceNumber("mockSequenceNumber");
    List<PutRecordsResultEntry> listSuccessfulPutRecordsResultEntry = new ArrayList<>();
    listSuccessfulPutRecordsResultEntry.add(putRecordsResultEntrySuccess);
    PutRecordsResult putRecordsResultSuccess = new PutRecordsResult();
    putRecordsResultSuccess.setFailedRecordCount(0);
    putRecordsResultSuccess.setRecords(listSuccessfulPutRecordsResultEntry);

    doReturn(putRecordsResultFailed).doReturn(putRecordsResultSuccess).when(spyKinesisProcessor)
        .getPutRecordsResultAfterPostingToKinesis(putRecordsRequest);


    spyKinesisProcessor.resendFailedRecordsToKinesis(putRecordsResult, listPutRecordsRequestEntry,
        putRecordsRequest);
    PutRecordsRequestEntry putRecordsReqEntryFailed = new PutRecordsRequestEntry();
    putRecordsReqEntryFailed.setData(ByteBuffer.wrap(new String("avroRecordFailed").getBytes()));
    List<PutRecordsRequestEntry> listPutRecordsRequestEntryFailed = new ArrayList<>();
    PutRecordsRequest putRecordsRequestForFailed = new PutRecordsRequest();
    listPutRecordsRequestEntryFailed.add(putRecordsReqEntryFailed);
    putRecordsRequestForFailed.setRecords(listPutRecordsRequestEntryFailed);
    verify(spyKinesisProcessor, atLeast(2)).validateResponse(any(), anyList(), any());
  }

}
