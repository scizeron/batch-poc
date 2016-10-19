package com.example.batch.process.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;

import com.example.batch.process.data.Mapping;

/**
 * 
 * Returns a fake mapping item, it should be read from a db regarding the clientId
 * 
 * @author 212552868
 *
 */
public class FakeMappingItemReader implements ItemReader<Mapping> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String clientId;
	
	private boolean done;
	
	public FakeMappingItemReader() {
		this.done = false;
	}
	
	@Value("#{jobParameters['clientId']}")
	public void setClient(String clientId) {
		this.clientId = clientId;
	}
	
	@Override
	public Mapping read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (this.done) {
			return null;
		}
		
		Mapping mapping = new Mapping();
		mapping.setClientId(clientId);
		logger.debug("Read mapping for '{}'", this.clientId);
		Thread.sleep(30000);
		this.done = true;
		return mapping;
	}

}
