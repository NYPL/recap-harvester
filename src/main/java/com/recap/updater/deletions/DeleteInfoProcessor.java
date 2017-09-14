package com.recap.updater.deletions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;

public class DeleteInfoProcessor implements Processor{
  
  boolean doCleanUp = false;
  
  private static Logger logger = LoggerFactory.getLogger(DeleteInfoProcessor.class);
  
  public DeleteInfoProcessor(boolean doCleanUp) {
    this.doCleanUp = doCleanUp;
  }

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
    try{
      File dirJsonFileThatHasDeleteInfo = exchange.getIn().getBody(File.class);
      
      if(dirJsonFileThatHasDeleteInfo != null){
        File[] files = dirJsonFileThatHasDeleteInfo.listFiles();
        
        List<String> deletedBibs = new ArrayList<>();
        List<String> deletedItems = new ArrayList<>();
        
        for(File file : files){
          if(file.getName().trim().endsWith(".json")){
            // add your logic here to create list of deleted bibs and items in marc in json format from the json file
            
            // add the marc-in-json bibs to deletedBibs
            
            // add the marc-in-json items to deletedItems 
          }
        }
        
        Map<String, List<String>> bibsAndItemsDeleted = new HashMap<>();
        bibsAndItemsDeleted.put(Constants.DELETED_BIBS, deletedBibs);
        bibsAndItemsDeleted.put(Constants.DELETED_ITEMS, deletedItems);
        
        if(doCleanUp){
          // make sure to delete the json files from local directory after processing 
        }
        
        exchange.getIn().setBody(bibsAndItemsDeleted, Map.class); 
      }
    }catch(Exception e){
     logger.error("Error occurred while extracting deleted bibs and items info from json file - ", e);
     throw new RecapHarvesterException("Error while extracting bibs and items info from json file");
    }
  }

}
