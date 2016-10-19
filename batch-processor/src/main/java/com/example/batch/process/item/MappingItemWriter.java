package com.example.batch.process.item;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;

import com.example.batch.process.data.Mapping;

/***
 * 
 * Collect the mapping object and put it in the job context execution
 * This part could be potentially kept for future 
 * 
 * @author 212552868
 *
 */
public class MappingItemWriter implements ItemWriter<Mapping> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private StepExecution stepExecution;
	
	@Override
	public void write(List<? extends Mapping> items) throws Exception {
		Mapping mapping = items.get(0);
		logger.debug("Write mapping for '{}'", mapping.getClientId());
		JobExecution jobContext = this.stepExecution.getJobExecution();
		jobContext.getExecutionContext().put("mapping", mapping);
		Thread.sleep(30000);
	}

	@BeforeStep
	public void setStepExecution(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

}
