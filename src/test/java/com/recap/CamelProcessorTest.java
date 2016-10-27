package com.recap;

import org.apache.camel.CamelContext;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.recap.config.BaseConfig;
import com.recap.updater.bib.BibJsonProcessor;
import com.recap.updater.bib.BibProcessor;
import com.recap.updater.bib.BibPublisher;
import com.recap.updater.bib.BibRecordProcessor;
import com.recap.utils.models.OAuth2Client;

public class CamelProcessorTest extends BaseTestCase {
	
    @Autowired
    CamelContext camelContext;
    
    ApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class);
    
    OAuth2Client nyplOAuth2Client = (OAuth2Client) context.getBean("oAuth2ClientNYPL");

    @Value("${scsbexportstaging.location}")
    private String scsbexportstaging;
    
    @Value("${nyplApiForBibs}")
	private String nyplApiForBibs;	
	
    @Test
    public void processSCSBExport() throws Exception {
   
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:" + scsbexportstaging + "?fileName=recapSample.xml&noop=true")
                        .split()
                        .tokenizeXML("bibRecord")
                .process(new BibRecordProcessor())
                .process(new BibProcessor())
                .process(new BibJsonProcessor())
                .process(new BibPublisher(nyplApiForBibs, nyplOAuth2Client, 
                		nyplOAuth2Client.createAndGetTokenAccessProperties()));
            }
        });

        Thread.sleep(300000);

    }
}