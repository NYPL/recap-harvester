package com.recap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.recap.exceptions.RecapHarvesterException;

@SpringBootApplication
public class HarvesterApplication {
	
	private static Logger logger = LoggerFactory.getLogger(HarvesterApplication.class);

	public static void main(String[] args) throws RecapHarvesterException {
		try{
			SpringApplication.run(HarvesterApplication.class, args);
		}catch(BeanCreationException beanException){
			logger.error("Bean creation exception occurred - ", beanException);
			throw new RecapHarvesterException(beanException.getMessage());
		}
		
	}
}
