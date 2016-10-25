package com.recap;

import com.recap.updater.BibJsonProcessor;
import com.recap.updater.BibProcessor;
import com.recap.updater.BibRecordProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
                from("file:" + scsbexportstaging + "?fileName=onerecord.xml&noop=true")
                        .split()
                        .tokenizeXML("bibRecord")
                .process(new BibRecordProcessor())
                .process(new BibProcessor())
                .process(new BibJsonProcessor());
            }
        });

        Thread.sleep(3000);

    }
}
