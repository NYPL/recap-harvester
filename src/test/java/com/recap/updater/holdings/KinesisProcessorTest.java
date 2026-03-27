package com.recap.updater.holdings;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.stream.KinesisProcessor;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResultEntry;

@ExtendWith(MockitoExtension.class)
public class KinesisProcessorTest {


    private List<byte[]> getListOfItems(int numOfItems) {
        List<byte[]> items = new ArrayList<>();
        for (int i = 0; i < numOfItems; i++) {
            items.add(new byte[i]);
        }
        return items;
    }

    @Test
    public void testBelowKinesisLimit() throws Exception {
        KinesisProcessor kinesisProcessor =
                spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
        doNothing().when(kinesisProcessor).sendToKinesis(anyList());

        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        List<byte[]> items = getListOfItems(10);
        exchange.getIn().setBody(items);

        kinesisProcessor.process(exchange);

        verify(kinesisProcessor, times(1)).sendToKinesis(anyList());
    }

    @Test
    public void testEqualToKinesisLimit() throws Exception {
        List<byte[]> items = getListOfItems(Constants.KINESIS_PUT_RECORDS_MAX_SIZE);
        KinesisProcessor kinesisProcessor =
                Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
        doNothing().when(kinesisProcessor).sendToKinesis(anyList());
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(items);

        kinesisProcessor.process(exchange);
        Mockito.verify(kinesisProcessor, times(1)).sendToKinesis(anyList());
    }

    @Test
    public void testAboveKinesisLimit() throws Exception {
        List<byte[]> items = getListOfItems(510);
        KinesisProcessor kinesisProcessor =
                Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
        doNothing().when(kinesisProcessor).sendToKinesis(anyList());
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody(items);

        kinesisProcessor.process(exchange);
        Mockito.verify(kinesisProcessor, times(2)).sendToKinesis(anyList());
    }

    @Test
    public void testMoreThanDoubleOfKinesisLimit() throws Exception {
        List<byte[]> items = getListOfItems(1050);
        KinesisProcessor kinesisProcessor =
                Mockito.spy(new KinesisProcessor(new BaseConfig(), "mockKinesisStream"));
        doNothing().when(kinesisProcessor).sendToKinesis(anyList());
        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
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
        PutRecordsRequest.Builder putRecordsRequest = PutRecordsRequest.builder();
        List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
        doReturn(putRecordsRequest.build()).when(spyKinesisProcessor)
                .createPutRecordsRequest(listPutRecordsRequestEntry, mockAvroRecords);
        PutRecordsResponse.Builder putRecordsResult = PutRecordsResponse.builder();
        putRecordsResult.failedRecordCount(0);
        doReturn(putRecordsResult.build()).when(spyKinesisProcessor)
                .getPutRecordsResultAfterPostingToKinesis(putRecordsRequest.build());
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
        List<PutRecordsRequestEntry> recordsOfPutRecordsReq = putRecordsRequest.records();
        Assert.assertTrue(recordsOfPutRecordsReq.size() == maxRecords ? true : false);
        PutRecordsRequestEntry putRecordsReqEntry = recordsOfPutRecordsReq.get(5);
        Assert.assertEquals("avroRecord6", new String(putRecordsReqEntry.data().asByteArray()));
        Assert.assertTrue(putRecordsReqEntry.partitionKey().length() > 0);
    }

    @Test
    public void testValidateResponse() throws RecapHarvesterException {
        KinesisProcessor kinesisProcessor = new KinesisProcessor(null, null);
        KinesisProcessor spyKinesisProcessor = Mockito.spy(kinesisProcessor);
        PutRecordsResponse.Builder putRecordsResult = PutRecordsResponse.builder();
        putRecordsResult.failedRecordCount(100);
        List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
        PutRecordsRequest putRecordsRequest = PutRecordsRequest.builder().build();
        doNothing().when(spyKinesisProcessor).resendFailedRecordsToKinesis(putRecordsResult.build(),
                listPutRecordsRequestEntry, putRecordsRequest);
        spyKinesisProcessor.validateResponse(putRecordsResult.build(), listPutRecordsRequestEntry,
                putRecordsRequest);
    }

    @Test
    public void testResendFailedRecordsToKinesis() throws RecapHarvesterException {
        BaseConfig mockConfig = Mockito.mock(BaseConfig.class);
        KinesisProcessor kinesisProcessor = new KinesisProcessor(mockConfig, null);
        KinesisProcessor spyKinesisProcessor = Mockito.spy(kinesisProcessor);
        List<byte[]> mockAvroRecords = new ArrayList<>();
        int maxRecords = 3;
        for (int i = 1; i <= maxRecords; i++) {
            mockAvroRecords.add(new String("avroRecord" + i).getBytes());
        }
        List<PutRecordsRequestEntry> listPutRecordsRequestEntry = new ArrayList<>();
        PutRecordsRequest putRecordsRequest =
                spyKinesisProcessor.createPutRecordsRequest(listPutRecordsRequestEntry, mockAvroRecords);
        listPutRecordsRequestEntry = putRecordsRequest.records();
        List<PutRecordsResultEntry> listPutRecordsResultEntry = new ArrayList<>();
        for (int i = 0; i < maxRecords; i++) {
            if (i == 1) {
                PutRecordsResultEntry.Builder putRecordsResultEntry = PutRecordsResultEntry.builder();
                putRecordsResultEntry.errorCode("mockErrorCode");
                listPutRecordsResultEntry.add(putRecordsResultEntry.build());
            } else {
                PutRecordsResultEntry.Builder putRecordsResultEntry = PutRecordsResultEntry.builder();
                putRecordsResultEntry.sequenceNumber("mockSequenceNumber-" + i);
                listPutRecordsResultEntry.add(putRecordsResultEntry.build());
            }
        }
        PutRecordsResponse.Builder putRecordsResult = PutRecordsResponse.builder();
        putRecordsResult.failedRecordCount(1);
        putRecordsResult.records(listPutRecordsResultEntry);


        PutRecordsResultEntry.Builder putRecordsResultEntryFailed = PutRecordsResultEntry.builder();
        putRecordsResultEntryFailed.errorCode("mockErrorCode");
        List<PutRecordsResultEntry> listFailedPutRecordsResultEntry = new ArrayList<>();
        listFailedPutRecordsResultEntry.add(putRecordsResultEntryFailed.build());
        PutRecordsResponse.Builder putRecordsResultFailed = PutRecordsResponse.builder();
        putRecordsResultFailed.failedRecordCount(1);
        putRecordsResultFailed.records(listFailedPutRecordsResultEntry);

        PutRecordsResultEntry.Builder putRecordsResultEntrySuccess = PutRecordsResultEntry.builder();
        putRecordsResultEntrySuccess.sequenceNumber("mockSequenceNumber");
        List<PutRecordsResultEntry> listSuccessfulPutRecordsResultEntry = new ArrayList<>();
        listSuccessfulPutRecordsResultEntry.add(putRecordsResultEntrySuccess.build());
        PutRecordsResponse.Builder putRecordsResultSuccess = PutRecordsResponse.builder();
        putRecordsResultSuccess.failedRecordCount(0);
        putRecordsResultSuccess.records(listSuccessfulPutRecordsResultEntry);

        doReturn(putRecordsResultFailed.build()).doReturn(putRecordsResultSuccess.build()).when(spyKinesisProcessor)
                .getPutRecordsResultAfterPostingToKinesis(any());

        spyKinesisProcessor.resendFailedRecordsToKinesis(putRecordsResult.build(), listPutRecordsRequestEntry,
                putRecordsRequest);
        PutRecordsRequestEntry.Builder putRecordsReqEntryFailed = PutRecordsRequestEntry.builder();
        putRecordsReqEntryFailed.data(SdkBytes.fromByteArray("avroRecordFailed".getBytes()));
        List<PutRecordsRequestEntry> listPutRecordsRequestEntryFailed = new ArrayList<>();
        PutRecordsRequest.Builder putRecordsRequestForFailed = PutRecordsRequest.builder();
        listPutRecordsRequestEntryFailed.add(putRecordsReqEntryFailed.build());
        putRecordsRequestForFailed.records(listPutRecordsRequestEntryFailed);
        verify(spyKinesisProcessor, atLeast(2)).validateResponse(any(), anyList(), any());
    }
}
