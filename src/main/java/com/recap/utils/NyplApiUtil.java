package com.recap.utils;

public class NyplApiUtil{
	
	private OAuth2Client oAuth2Client;
	
	private TokenProperties tokenProperties;
	
	private String nyplApiForBibs;

	public OAuth2Client getoAuth2Client() {
		return oAuth2Client;
	}

	public void setoAuth2Client(OAuth2Client oAuth2Client) {
		this.oAuth2Client = oAuth2Client;
	}

	public TokenProperties getTokenProperties() {
		return tokenProperties;
	}

	public void setTokenProperties(TokenProperties tokenProperties) {
		this.tokenProperties = tokenProperties;
	}

	public String getNyplApiForBibs() {
		return nyplApiForBibs;
	}

	public void setNyplApiForBibs(String nyplApiForBibs) {
		this.nyplApiForBibs = nyplApiForBibs;
	}
	

}
