package com.recap.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecapHarvesterException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(RecapHarvesterException.class);
	
	public RecapHarvesterException(String message){
		logger.error("RecapHarvesterException occurred - " + message);
	}

}
