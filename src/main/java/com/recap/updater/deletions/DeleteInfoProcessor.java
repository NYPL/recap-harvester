package com.recap.updater.deletions;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DeleteInfoProcessor implements Processor {

  boolean doCleanUp = false;

  private static Logger logger = LoggerFactory.getLogger(DeleteInfoProcessor.class);

  public DeleteInfoProcessor(boolean doCleanUp) {
    this.doCleanUp = doCleanUp;
  }
  
  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
    try {
      File dirJsonFileThatHasDeleteInfo = exchange.getIn().getBody(File.class);

      if (dirJsonFileThatHasDeleteInfo != null) {
        File[] files = dirJsonFileThatHasDeleteInfo.listFiles();

        List<String> deletedBibs = new ArrayList<>();
        List<String> deletedItems = new ArrayList<>();

        for (File file : files) {
          if (file.getName().trim().endsWith(".json")) {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Map<String, Object>>> deletedJSON = mapper.readValue(file,
                mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            for (Map<String, Map<String, Object>> bib : deletedJSON) {
            	
              Boolean deleteAllItems = (Boolean) bib.get("bib").get("deleteAllItems");

              if (deleteAllItems) {
                // Go to Bib Service and get items for the bib.
                // for each Item response
                // push a JSON string that looks like this to deletedItems:
                // {
                // "id": "10000001",
                // "deletedDate": "2011-02-28",
                // "deleted": true
                // },

                // Does this also need to insert something into
                // deletedBibs?
              } else {            	
            	 List<Map<String, String>> items = (List) bib.get("bib").get("items");
            	 
            	 for (Map<String, String> item :  items) {
            		deletedItems.add(transformToDeleted(item, "item", (String) bib.get("bib").get("owningInstitutionCode"))); 
            	 }
            	 logger.info("deleting individual items for " + bib.get("bib").get("owningInstitutionCode") + "-" + bib.get("bib").get("owningInstitutionBibId"));
              }

            }
          }
          if (doCleanUp) {
            file.delete();
          }
        }

        Map<String, List<String>> bibsAndItemsDeleted = new HashMap<>();
        bibsAndItemsDeleted.put(Constants.DELETED_BIBS, deletedBibs);
        bibsAndItemsDeleted.put(Constants.DELETED_ITEMS, deletedItems);

        exchange.getIn().setBody(bibsAndItemsDeleted, Map.class);
      }
    } catch (Exception e) {
      logger.error("Error occurred while extracting deleted bibs and items info from json file - ",
          e);
      throw new RecapHarvesterException(
          "Error while extracting bibs and items info from json file");
    }
  }

  private String transformToDeleted(Map<String, String> record, String bibOrItem, String owningInstitution) throws JsonGenerationException, JsonMappingException, IOException {
	
	Map<String, Object> deletedRecord = new HashMap<>();
	DateFormat deleteDateFormat = new SimpleDateFormat("YYYY-MM-DD");
	
	deletedRecord.put("nyplSource", "recap-" + owningInstitution.toLowerCase());
	deletedRecord.put("nyplType", bibOrItem);
	deletedRecord.put("deletedDate", deleteDateFormat.format(new Date()));
	deletedRecord.put("deleted", true);
	deletedRecord.put("fixedFields", new HashMap<>());
	deletedRecord.put("varFields", new ArrayList<>());

	if (bibOrItem == "bib") {
		deletedRecord.put("id", record.get("owningInstitutionBibId"));
	} else {
		deletedRecord.put("id", record.get("owningInstitutionItemId"));
		deletedRecord.put("bibIds", new ArrayList<>());
	}
	
	return new ObjectMapper().writeValueAsString(deletedRecord);	  
  }

}
