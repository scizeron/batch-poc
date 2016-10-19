package com.example.batch.process;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.batch.process.v1.JobEvent;

@Service("asyncJobHandler")
public class AsyncJobHandlerImpl implements JobHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private JobLauncher jobLauncher;
	
//	@Autowired
//	@Qualifier("extractJob")
//	private Job extractJob;
//	
	@Autowired
	private BatchProcessorConfiguration batchProcessorConfiguration;
	
	@Async
    public void handleJob(JobEvent jobEvent) {
		Map<String, JobParameter> parameters = new HashMap<String, JobParameter>();
		parameters.put("jobName", new JobParameter(jobEvent.getJobName()));
		jobEvent.getJobParameters().keySet().stream().forEach(key -> {
			parameters.put(key, new JobParameter(jobEvent.getJobParameters().get(key)));
		});
		parameters.put("run.id", new JobParameter(UUID.randomUUID().toString()));
		
		// to have a different job name in job_execution table instead of having extract job for all
		Job extractJob = this.batchProcessorConfiguration.getExtractJob(jobEvent.getJobName());
		
		logger.debug("Handle job '{}' with {}.", extractJob.getName(), parameters);
	
		try {
			this.jobLauncher.run(extractJob, new JobParameters(parameters));
			
		} catch (JobExecutionAlreadyRunningException e) {
			logger.error("Job '{}' is already running", extractJob.getName(), e);
		
		} catch (JobRestartException e) {
			logger.error("Job '{}' cannot be restarted", extractJob.getName(), e);
		
		} catch (JobInstanceAlreadyCompleteException e) {
			logger.error("Job '{}' is already completed", extractJob.getName(), e);
		
		} catch (JobParametersInvalidException e) {
			logger.error("Job '{}' has received invalid parameter(s)", extractJob.getName(), e);
		}
	}
}
