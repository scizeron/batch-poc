package com.example.batch.scheduler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author 212552868
 * BatchJob is deserialized and instanciated by Quartz. Need to collect the configured the  rest template instance
 *
 */
@Component
public class BatchJob implements Job {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RestTemplate restTemplate;
	
	private static RestTemplate REST_TEMPLATE = null;

	private static String PROCESSING_SERVICE = null;
	
	@Value("${batch.processing.service:localhost}")
    private String processingService;
	
	 @PostConstruct
	 public void postContruct() {
		 REST_TEMPLATE = this.restTemplate;
		 
		 if ("localhost".equals(this.processingService)) {
			 this.processingService = "localhost:7001";
		 }
		 
		 PROCESSING_SERVICE = this.processingService;
	 }
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.debug("execute {}", context);
		
		final JobDetail jobDetail = context.getJobDetail();
		final String jobName = jobDetail.getKey().getName();
		
		try {
			Map<String, String> jobParameters = new HashMap<>();
			JobDataMap jobDataMap = jobDetail.getJobDataMap();
			String[] keys = jobDataMap.getKeys();
			if (keys != null) {
				for (String key : keys) {
					jobParameters.put(key, jobDataMap.getString(key));
				}
			}
			
			ResponseEntity<String> exchange = REST_TEMPLATE.exchange(
					  String.format("http://%s/v1/asyncjobs/%s", PROCESSING_SERVICE , jobName)
					, HttpMethod.POST
					, new HttpEntity<Map<String, String>>(jobParameters)
					, String.class);
			
			if (!exchange.getStatusCode().equals(HttpStatus.ACCEPTED)) {
				throw new JobExecutionException(String.format("Error while submitting '%s', status : %d", jobName, exchange.getStatusCode().value() ) );
			}
			
		} catch (RestClientException restClientException) {
			logger.error("Error while submitting '" + jobName  + "'");
			throw new JobExecutionException("Error while submitting '" + jobName  + "'", restClientException);
		}
	}
}
