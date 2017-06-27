package com.recap.updater.holdings;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.test.junit4.ExchangeTestSupport;
import org.junit.Test;
import org.mockito.Mockito;

import com.recap.config.BaseConfig;
import com.recap.constants.Constants;

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
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(new BaseConfig()));
    doReturn(true).when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(1)).sendToKinesis(anyList());
  }

  @Test
  public void testEqualToKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(Constants.KINESIS_PUT_RECORDS_MAX_SIZE);
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(new BaseConfig()));
    doReturn(true).when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(1)).sendToKinesis(anyList());
  }

  @Test
  public void testAboveKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(510);
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(new BaseConfig()));
    doReturn(true).when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(2)).sendToKinesis(anyList());
  }

  @Test
  public void testMoreThanDoubleOfKinesisLimit() throws Exception {
    setUp();
    List<byte[]> items = getListOfItems(1050);
    KinesisProcessor kinesisProcessor = Mockito.spy(new KinesisProcessor(new BaseConfig()));
    doReturn(true).when(kinesisProcessor).sendToKinesis(anyList());
    exchange.getIn().setBody(items);
    kinesisProcessor.process(exchange);
    Mockito.verify(kinesisProcessor, times(3)).sendToKinesis(anyList());
  }

}
