package com.example.batch.process.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;

import com.example.batch.process.data.DbRecord;
import com.example.batch.process.data.Mapping;

/**
 * Should be replaced by a db reader, taking as incoming parameter the mapping entry.
 * 
 * @author 212552868
 *
 */
public class FakeDbItemReader implements ItemReader<DbRecord> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private StepExecution stepExecution;
	
	private String clientId;
	
	private boolean done;
	
	@Value("#{jobParameters['clientId']}")
	public void setClient(String clientId) {
		this.clientId = clientId;
	}
	
	public FakeDbItemReader() {
		this.done = false;
	}
	
	@BeforeStep
    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
	
	@Override
	public DbRecord read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (this.done) {
			return null;
		}
		
		JobExecution jobContext = this.stepExecution.getJobExecution();
		Mapping mapping = (Mapping) jobContext.getExecutionContext().get("mapping");
		
		if (mapping == null) {
			logger.error("The expected mapping for '{}' is missing", clientId);
			this.stepExecution.getJobExecution().setExitStatus(ExitStatus.STOPPED);
			return null;
		}
		
		logger.debug("Read dbRecords related to '{}'", mapping.getClientId());
		Thread.sleep(5000);
		this.done = true;
		DbRecord dbRecord = new DbRecord();
		return dbRecord;
	}

}
