package com.recap.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BibFieldProcessingException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(BibFieldProcessingException.class);
	
	public BibFieldProcessingException(String message){
		logger.error("BibFieldProcessing caused an exception - " + message);
	}

}
