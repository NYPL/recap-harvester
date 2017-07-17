package com.recap.updater.deletions;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DeleteProcessor implements Processor{

  @Override
  public void process(Exchange exchange) throws Exception {
    System.out.println("Received json file for processing");
  }

}
