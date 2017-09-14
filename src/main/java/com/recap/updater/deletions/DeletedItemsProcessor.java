package com.recap.updater.deletions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Item;

public class DeletedItemsProcessor implements Processor{
  
  private static Logger logger = LoggerFactory.getLogger(DeletedItemsProcessor.class);

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
   try{
     Map<String, List<String>> deletedBibsAndItemsInMarcJson = exchange.getIn().getBody(Map.class);
     if(deletedBibsAndItemsInMarcJson != null){
       List<String> itemsMarcInJson = deletedBibsAndItemsInMarcJson.get(Constants.DELETED_ITEMS);
       List<Item> deletedItems = new ArrayList<>();
       for(String itemInJson : itemsMarcInJson){
         Item item = new ObjectMapper().readValue(itemInJson, Item.class);
         deletedItems.add(item);
       }
       exchange.getIn().setBody(deletedItems, List.class);
     }
   }catch(Exception e){
     logger.error("Error while converting marc in json item to item object - ", e);
     throw new RecapHarvesterException("Error while converting marc json item to item object");
   }
  }

}
