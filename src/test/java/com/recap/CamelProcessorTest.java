package com.recap;

import com.recap.updater.RecapXmlProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by peris on 10/21/16.
 */
public class CamelProcessorTest extends BaseTestCase {

    @Autowired
    CamelContext camelContext;

    @Value("${scsbexportstaging.location}")
    private String scsbexportstaging;

    @Test
    public void processSCSBExport() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:" + scsbexportstaging)
                        .split()
                        .tokenizeXML("bibRecord")

                .process(new RecapXmlProcessor());
            }
        });

        Thread.sleep(3000);

    }
}
