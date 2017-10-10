package com.recap.updater.deletions;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class OAuth2Client {

  private String accessTokenUri;

  private String clientId;

  private String clientSecret;

  private String grantType;

  public OAuth2Client(String accessTokenUrl, String clientId, String clientSecret,
      String grantType) {
    this.accessTokenUri = accessTokenUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.grantType = grantType;
  }

  public OAuth2ProtectedResourceDetails getClientCredentialsResourceDetails() {
    ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
    resource.setAccessTokenUri(accessTokenUri);
    resource.setClientId(clientId);
    resource.setClientSecret(clientSecret);
    resource.setGrantType(grantType);

    return resource;
  }

  public OAuth2RestTemplate getOAuth2RestTemplate() {
    OAuth2RestTemplate oAuth2RestTemplate =
        new OAuth2RestTemplate(getClientCredentialsResourceDetails());

    return oAuth2RestTemplate;
  }

  public OAuth2AccessToken createAndGetOAuth2AccessToken() {
    OAuth2RestTemplate oAuth2RestTemplate = getOAuth2RestTemplate();
    OAuth2AccessToken oAuth2AccessToken = oAuth2RestTemplate.getAccessToken();

    return oAuth2AccessToken;
  }

}
