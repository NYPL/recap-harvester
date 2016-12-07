package com.recap.updater.bib;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.recap.constants.Constants;
import com.recap.utils.NyplApiUtil;
import com.recap.utils.OAuth2Client;
import com.recap.utils.TokenProperties;

public class BibPublisher implements Processor{
	
	private static Logger logger = LoggerFactory.getLogger(BibPublisher.class.getName());
	
	private OAuth2Client nyplOAuthClient;
	
	private String nyplApiForBibs;
	
	private TokenProperties tokenProperties;

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> exchangeContents = (Map<String, Object>) exchange.getIn().getBody();
		String bibContent = (String) exchangeContents.get(Constants.BIB_JSON);
		NyplApiUtil nyplApiUtil = (NyplApiUtil) exchangeContents.get(Constants.API_UTIL);
		nyplOAuthClient = nyplApiUtil.getoAuth2Client();
		nyplApiForBibs = nyplApiUtil.getNyplApiForBibs();
		tokenProperties = nyplApiUtil.getTokenProperties();
		postBibInfoToApi(tokenProperties, nyplApiForBibs, bibContent, nyplOAuthClient);
		System.out.println(bibContent);
		exchange.getIn().setBody(exchangeContents);
	}
	
	public void postBibInfoToApi(TokenProperties nyplTokenProperties, String bibsApi, String bibsJson, 
			OAuth2Client nyplOAuthClient) throws URISyntaxException{
        String tokenValue = nyplTokenProperties.getTokenValue();
        Date tokenExpiration = nyplTokenProperties.getTokenExpiration();
        String tokenType = nyplTokenProperties.getTokenType();
        System.out.println("Token Value - " + tokenValue);
        System.out.println("Token Type - " + tokenType);
        System.out.println("Token Expiration - " + tokenExpiration);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(bibsApi);
        URI uri = new URI(builder.toUriString());
        RequestEntity<String> requestEntity = RequestEntity.post(uri).contentType(MediaType.APPLICATION_JSON).
        		body(bibsJson);
        System.out.println("headers - " + requestEntity.getHeaders());
        try{
        	ResponseEntity response = nyplOAuthClient.getOAuth2RestTemplate().exchange(uri, HttpMethod.POST, 
            		requestEntity, String.class);
            System.out.println(response.getBody());
            System.out.println(response.getStatusCode());
            System.out.println(response.getHeaders());
        }catch(Exception e){
        	logger.error("Error occurred while calling bib api - ", e);
        }
        logger.info("Published bib - " + bibsJson);
        
	}
	
	

}
