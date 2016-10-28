package com.recap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.recap.utils.OAuth2Client;

@Configuration
@PropertySource("classpath:application.properties")
public class BaseConfig {
	
	@Bean
	public OAuth2Client oAuth2ClientNYPL(){
		return new OAuth2Client();
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
