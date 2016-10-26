package com.recap.utils.models;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class OAuth2Client {
    
    @Value("${accessTokenUrl}")
	private String accessTokenUri;
	
	@Value("${clientId}")
	private String clientId;
	
	@Value("${clientSecret}")
	private String clientSecret;
	
	@Value("${grantType}")
	private String grantType;
	
	public OAuth2ProtectedResourceDetails getClientCredentialsResourceDetails() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setAccessTokenUri(accessTokenUri);
		resource.setClientId(clientId);
		resource.setClientSecret(clientSecret);
		resource.setGrantType(grantType);

		return resource;
	}

	public OAuth2RestTemplate getOAuth2RestTemplate() {
		OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(getClientCredentialsResourceDetails());

		return oAuth2RestTemplate;
	}
	
	public TokenProperties getTokenAccessProperties(){
        OAuth2RestTemplate oAuth2RestTemplate = getOAuth2RestTemplate();
        OAuth2AccessToken oAuth2AccessToken = oAuth2RestTemplate.getAccessToken();
        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setTokenValue(oAuth2AccessToken.getValue());
        tokenProperties.setTokenType(oAuth2AccessToken.getTokenType());
        tokenProperties.setTokenExpiration(oAuth2AccessToken.getExpiration());
        tokenProperties.setoAuth2RestTemplate(oAuth2RestTemplate);
        return tokenProperties;
	}

}
