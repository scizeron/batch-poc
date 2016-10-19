package com.example.batch.process.item;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import com.example.batch.process.data.DbRecord;

/**
 * Should be replaced by a Azure Storage writer
 * 
 * @author 212552868
 *
 */
public class FakeDbItemWriter implements ItemWriter<DbRecord> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String clientId;
	
	@Value("#{jobParameters['clientId']}")
	public void setClient(String clientId) {
		this.clientId = clientId;
	}
	
	@Override
	public void write(List<? extends DbRecord> items) throws Exception {
		logger.debug("Write items for '{}'", this.clientId);
		Thread.sleep(5000);		
	}

}
