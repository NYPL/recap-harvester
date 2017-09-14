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
import com.recap.models.Bib;

public class DeletedBibsProcessor implements Processor{
  
  private static Logger logger = LoggerFactory.getLogger(DeletedBibsProcessor.class);

  @Override
  public void process(Exchange exchange) throws RecapHarvesterException {
    try{
      Map<String, List<String>> deletedBibsAndItemsInMarcJson = exchange.getIn().getBody(Map.class);
      if(deletedBibsAndItemsInMarcJson != null){
        List<String> bibsMarcInJson = deletedBibsAndItemsInMarcJson.get(Constants.DELETED_BIBS);
        List<Bib> deletedBibs = new ArrayList<>();
        for(String bibInJson : bibsMarcInJson){
          Bib bib = new ObjectMapper().readValue(bibInJson, Bib.class);
          deletedBibs.add(bib);
        }
        exchange.getIn().setBody(deletedBibs, List.class);
      }
    }catch(Exception e){
      logger.error("Error while converting marc in json bibs to bib objects - ", e);
      throw new RecapHarvesterException("Error while converting json to bib object");
    }
  }

}
