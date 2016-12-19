package com.recap.updater.holdings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.recap.constants.Constants;
import com.recap.models.Bib;
import com.recap.models.Item;
import com.recap.utils.NyplApiUtil;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProperties;

public class ItemsJsonProcessor implements Processor {

	private OAuth2Client nyplOAuthClient;

	private String nyplApiForItems;

	private TokenProperties tokenProperties;
	
	private static final Logger logger = LoggerFactory.getLogger(ItemsJsonProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		List<Item> items = (List<Item>) exchangeContents.get(Constants.LIST_ITEMS);
		List<String> itemsAsJson = getItemsAsJson(items);
		exchange.getIn().setBody(itemsAsJson);
	}
	
	public List<String> getItemsAsJson(List<Item> items) 
			throws JsonGenerationException, JsonMappingException, IOException{
		List<String> itemsAsJson = new ArrayList<>();
		for(Item item : items){
			itemsAsJson.add(new ObjectMapper().writeValueAsString(item));
		}
		return itemsAsJson;
	}
	
	public void postItemInfoToApi(TokenProperties nyplTokenProperties, String itemsApi, List<Item> items, 
			OAuth2Client nyplOAuthClient) throws URISyntaxException, JsonGenerationException, 
	JsonMappingException, IOException{
		for(Item item : items){
			String itemAsJson = new ObjectMapper().writeValueAsString(item);
			String tokenValue = nyplTokenProperties.getTokenValue();
	        Date tokenExpiration = nyplTokenProperties.getTokenExpiration();
	        String tokenType = nyplTokenProperties.getTokenType();
	        System.out.println("Token Value - " + tokenValue);
	        System.out.println("Token Type - " + tokenType);
	        System.out.println("Token Expiration - " + tokenExpiration);
	        
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(itemsApi);
	        URI uri = new URI(builder.toUriString());
	        RequestEntity<String> requestEntity = RequestEntity.post(uri).contentType(MediaType.APPLICATION_JSON)
	        		.body(itemAsJson);
	        System.out.println("headers - " + requestEntity.getHeaders());
	        try{
	        	ResponseEntity response = nyplOAuthClient.getOAuth2RestTemplate().exchange(uri, HttpMethod.POST, 
		        		requestEntity, String.class);
		        System.out.println(response.getBody());
		        System.out.println(response.getStatusCode());
		        System.out.println(response.getHeaders());
	        }catch(Exception e){
	        	logger.error("Error while calling items api - ", e);
	        }
	        logger.info("Published item - " + item.getId());
		}
	}

}
