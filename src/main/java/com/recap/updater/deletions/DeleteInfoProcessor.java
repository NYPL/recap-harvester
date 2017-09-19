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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonParseException;
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

              Map<String, Object> theBib = bib.get("bib");

              Boolean deleteAllItems = (Boolean) theBib.get("deleteAllItems");

              if (deleteAllItems) {
                List<String> itemIds = getItemIds(theBib.get("owningInstitutionCode").toString(), theBib.get("owningInstitutionBibId").toString());
                
                // First delete all items for the bib                
                for (String itemId : itemIds) {
                  Map<String, String> itemRecord = new HashMap<>();
                  itemRecord.put("owningInstitutionItemId", itemId);
                  itemRecord.put("owningInstitutionBibId",theBib.get("owningInstitutionBibId").toString());
                  deletedItems.add(transformToDeleted(itemRecord, "item", theBib.get("owningInstitutionCode").toString()));
                }
                
                // Then delete the bib itself                 
                Map<String, String> bibRecord = new HashMap<>();
                bibRecord.put("owningInstitutionBibId",theBib.get("owningInstitutionBibId").toString());
                deletedBibs.add(transformToDeleted(bibRecord, "bib", theBib.get("owningInstitutionCode").toString()));
              } else {
                List<Map<String, String>> items = (List) theBib.get("items");

                for (Map<String, String> item : items) {
                  deletedItems.add(transformToDeleted(item, "item",
                      (String) theBib.get("owningInstitutionCode")));
                }
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
  
  private List<String> getItemIds(String owningInstitutionCode, String owningInstitutionBibId) throws RecapHarvesterException{
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Authorization", "Bearer TOKEN HERE");
    HttpEntity<String> httpEntity = new HttpEntity<>("parameters", httpHeaders);
    String url = "https://API-DOMAIN/api/v0.1/bibs/recap-" + owningInstitutionCode.toLowerCase() + "/" + owningInstitutionBibId + "/items";
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
    List<String> itemIds = new ArrayList<>();
    try {
      Map<String, List<Map<String, Object>>> data =
          new ObjectMapper().readValue(response.getBody(), Map.class);
      List<Map<String, Object>> items = data.get("data");
      
      for (Map<String, Object> item : items) {
       itemIds.add(item.get("id").toString());
      }
    } catch (JsonMappingException e) {
      String errMessage = "JSON mapping failed while getting item ids " + e.getMessage();
      logger.error(errMessage, e);
      throw new RecapHarvesterException(errMessage);
    } catch (Jsonpar e) {
      String errMessage = "JSON parsing failed while getting item ids " + e.getMessage();
      logger.error(errMessage, e);
      throw new RecapHarvesterException(errMessage);
    }
    
    return itemIds;
  }

  private String transformToDeleted(Map<String, String> record, String bibOrItem,
      String owningInstitution) throws JsonGenerationException, JsonMappingException, IOException {

    Map<String, Object> deletedRecord = new HashMap<>();
    DateFormat deleteDateFormat = new SimpleDateFormat("YYYY-MM-dd");

    deletedRecord.put("nyplSource", "recap-" + owningInstitution.toLowerCase());
    deletedRecord.put("nyplType", bibOrItem);
    deletedRecord.put("deletedDate", deleteDateFormat.format(new Date()));
    deletedRecord.put("deleted", true);
    deletedRecord.put("fixedFields", new HashMap<>());
    deletedRecord.put("varFields", new ArrayList<>());

    if (bibOrItem == "bib") {
      deletedRecord.put("id", record.get("owningInstitutionBibId"));
      logger.info(
          "deleting the bib " + owningInstitution + "-" + record.get("owningInstitutionBibId"));
    } else {
      logger.info("deleting the individual item: " + owningInstitution + "-"
          + record.get("owningInstitutionItemId"));
      deletedRecord.put("id", record.get("owningInstitutionItemId"));
      deletedRecord.put("bibIds", new ArrayList<>());
    }

    return new ObjectMapper().writeValueAsString(deletedRecord);
  }

}
