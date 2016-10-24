package com.recap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recap.models.Bib;
import com.recap.updater.RecapXmlProcessor;
import com.recap.xml.models.BibRecord;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

public class ProcessorTest {

    private static Logger logger = Logger.getLogger(ProcessorTest.class);
    private RecapXmlProcessor processor = new RecapXmlProcessor();

    @Test
    public void readXMLFileContents() throws IOException, URISyntaxException {
        URL resource = getClass().getResource("onerecord.xml");
        File file = new File(resource.toURI());
        String xmlFileContents = FileUtils.readFileToString(file);

        assertNotNull(xmlFileContents);
    }

    @Test
    public void testIfBibRecordsAreReturned() throws Exception {
        List<BibRecord> bibRecords;
        URL resource = getClass().getResource("onerecord.xml");
        File file = new File(resource.toURI());
        String xmlFileContents = FileUtils.readFileToString(file);

        bibRecords = processor.getBibRecord(xmlFileContents);
        System.out.println("Total BibRecords - " + bibRecords.size());
        assertTrue(bibRecords.size() >= 1);
    }

	@Test
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
	}

}
