package com.recap.updater.holdings;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.test.junit4.ExchangeTestSupport;
import org.junit.Test;
import org.mockito.Mockito;

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;

public class KinesisProcessorTest extends ExchangeTestSupport {


  private List<byte[]> getListOfItems(int numOfItems) {
    List<byte[]> items = new ArrayList<>();
    for (int i = 0; i < numOfItems; i++) {
      items.add(new byte[i]);
    }
    return items;
  }

  @Test
  public void processTestLessThanKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(Constants.KINESIS_PUT_RECORDS_MAX_SIZE);
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(new BaseConfig()));
    doReturn(true).when(kinesisProcessor).sendToKinesis(items);
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(1)).sendToKinesis(items);
  }

  @Test
  public void processTestMoreThanKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(510);
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(mock(BaseConfig.class)));
    doNothing().when(kinesisProcessor).splitItemsAndSendToKinesis(items);
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    verify(kinesisProcessor, times(1)).splitItemsAndSendToKinesis(items);
  }

  @Test
  public void testSplitItemsAndSendToKinesis() throws RecapHarvesterException {
    List<byte[]> items = getListOfItems(1050);
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(mock(BaseConfig.class)));
    doReturn(true).when(kinesisProcessor).sendToKinesis(anyList());
    kinesisProcessor.splitItemsAndSendToKinesis(items);
    verify(kinesisProcessor, times(3)).sendToKinesis(anyList());
  }

}
