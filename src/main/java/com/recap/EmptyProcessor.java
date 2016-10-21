package com.recap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.beans.PropertyChangeListener;

/**
 * Created by peris on 10/21/16.
 */
public class EmptyProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {

    }
}
