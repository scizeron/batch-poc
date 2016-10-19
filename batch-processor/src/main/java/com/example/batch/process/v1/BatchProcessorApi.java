package com.example.batch.process.v1;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.batch.process.JobHandler;

@RestController
@RequestMapping(value="/v1", produces={MediaType.APPLICATION_JSON_VALUE})
public class BatchProcessorApi {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	@Qualifier("asyncJobHandler")
	private JobHandler asyncJobHandler;
	
	@RequestMapping(value="/asyncjobs/{jobName}", method={RequestMethod.POST}, consumes={MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void asyncJobExecution(@PathVariable("jobName") String jobName, @RequestBody JobExecutionBody jobExecution) {
		logger.debug("Execute job '{}' with {}.", jobName, ToStringBuilder.reflectionToString(jobExecution, ToStringStyle.JSON_STYLE));
		this.asyncJobHandler.handleJob(new JobEvent(jobName, jobExecution.getValues()));
	}
}
