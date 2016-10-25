package com.recap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.models.Bib;
import com.recap.updater.BibJsonProcessor;
import com.recap.updater.BibProcessor;
import com.recap.updater.BibRecordProcessor;
import com.recap.xml.models.BibRecord;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ExpressionNode;
import org.apache.camel.model.RouteBuilderDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

public class ProcessorTest extends BaseTestCase{

    private static Logger logger = Logger.getLogger(ProcessorTest.class);
    private BibRecordProcessor processor = new BibRecordProcessor();
    
    @Autowired
    private CamelContext camelContext;

    @Test
    public void readXMLFileContents() throws IOException, URISyntaxException {
        URL resource = getClass().getResource("onerecord.xml");
        File file = new File(resource.toURI());
        String xmlFileContents = FileUtils.readFileToString(file, "UTF-8");

        assertNotNull(xmlFileContents);
    }

    @Test
    public void testIfBibRecordIsReturned() throws Exception {
        BibRecord bibRecord;
        URL resource = getClass().getResource("onerecord.xml");
        File file = new File(resource.toURI());
        //String xmlFileContent = FileUtils.readFileToString(file, "UTF-8");
        camelContext.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("file:" + file.getCanonicalPath() + "&noop=true")
				.split().tokenize("bibRecord")
				.process(new BibRecordProcessor())
				.process(new BibProcessor())
				.process(new BibJsonProcessor());
			}
		});
        
       // assertTrue(bibRecord != null);
    }

	/*@Test
	public void testIfBibIsReturned() throws Exception{
        List<BibRecord> bibRecords;
        URL resource = getClass().getResource("onerecord.xml");
        File file = new File(resource.toURI());
        String xmlFileContents = FileUtils.readFileToString(file);

        bibRecords = processor.getBibRecord(xmlFileContents);
        System.out.println("Total BibRecords - " + bibRecords.size());
        assertTrue(bibRecords.size() >= 1);

		BibRecord bibRecord = bibRecords.get(0);
		Bib bib = processor.getBibFromBibRecord(bibRecord);
		assertNotNull(bib);
	}


	@Test
	public void testIfBibsInValidJsonFormat() throws Exception{
        List<BibRecord> bibRecords;
        URL resource = getClass().getResource("onerecord.xml");
        File file = new File(resource.toURI());
        String xmlFileContents = FileUtils.readFileToString(file);

        bibRecords = processor.getBibRecord(xmlFileContents);
        System.out.println("Total BibRecords - " + bibRecords.size());
        assertTrue(bibRecords.size() >= 1);

        BibRecord bibRecord = bibRecords.get(0);
        Bib bib = processor.getBibFromBibRecord(bibRecord);

		List<Bib> bibs = new ArrayList<>();
		bibs.add(bib);
		List<String> jsonBibs = processor.getListOfBibsAsJSON(bibs);
		System.out.println(jsonBibs.get(0));
		Bib actualBib = new ObjectMapper().readValue(jsonBibs.get(0).getBytes(), Bib.class);
		Assert.assertEquals(actualBib.getId(), bib.getId());
	}*/

}
