package com.recap.updater.holdings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.recap.constants.Constants;
import com.recap.exceptions.RecapHarvesterException;
import com.recap.models.Item;

public class ItemsJsonProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(ItemsJsonProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws RecapHarvesterException {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		List<Item> items = (List<Item>) exchangeContents.get(Constants.LIST_ITEMS);
		List<String> itemsAsJson = getItemsAsJson(items);
		exchange.getIn().setBody(itemsAsJson);
	}
	
	public List<String> getItemsAsJson(List<Item> items) 
			throws RecapHarvesterException{
		try{
			List<String> itemsAsJson = new ArrayList<>();
			for(Item item : items){
				itemsAsJson.add(new ObjectMapper().writeValueAsString(item));
			}
			return itemsAsJson;
		}catch(JsonGenerationException jsonException){
			logger.error("JsonGenerationException occurred while converting items to json - ",
					 jsonException);
			throw new RecapHarvesterException("JsonGenerationException occurred while converting "
					+ "item to json" + jsonException.getMessage());
		}catch(JsonMappingException jsonException){
			logger.error("JsonMappingException occurred while converting items to json - ",
					 jsonException);
			throw new RecapHarvesterException("JsonMappingException occurred while converting "
					+ "item to json" + jsonException.getMessage());
		}catch(IOException ioe){
			logger.error("IOException occurred while converting items to json - ",
					 ioe);
			throw new RecapHarvesterException("IOException occurred while converting "
					+ "item to json" + ioe.getMessage());
		}
	}

}
