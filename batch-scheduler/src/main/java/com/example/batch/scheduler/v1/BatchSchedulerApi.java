package com.example.batch.scheduler.v1;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.batch.scheduler.BatchJob;

@RestController
@RequestMapping(value="/v1", produces={MediaType.APPLICATION_JSON_VALUE})
public class BatchSchedulerApi {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private Scheduler scheduler;
	
	@RequestMapping("/home")
	public String home() {
		return "Hello world";
	}
	
	@RequestMapping(value="/jobs", method={RequestMethod.POST}, consumes={MediaType.APPLICATION_JSON_VALUE})
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> create(@RequestBody SaveOrUpdateJobPayload newPayload) throws SchedulerException {
		// just an 1:1 link between job and trigger
		
		JobDataMap newJobDataMap = new JobDataMap();
		newJobDataMap.put("clientId", newPayload.getClientId());
				
		// add more data if needed ...
		try {
			this.scheduler.scheduleJob(
					newJob(BatchJob.class)
						.usingJobData(newJobDataMap)
						.withIdentity(newPayload.getJobName())
						.withDescription(newPayload.getDescription())
						.build()
					, newTrigger()
				    	.withIdentity("tr_" + newPayload.getJobName())
					    .withSchedule(CronScheduleBuilder.cronSchedule(newPayload.getCronExpression()))
					    .forJob(newPayload.getJobName())
					    .build()
				    );
			return ResponseEntity.status(HttpStatus.CREATED).build();
			
		} catch(ObjectAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response().withMessage(e.getMessage()));
		}
	}
	
	@RequestMapping(value="/jobs/{jobName}", method={RequestMethod.GET}, consumes={MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> get(@PathVariable("jobName") String jobName) throws SchedulerException {
		JobPayload job = convertJobPayloadToJobDetail(this.scheduler.getJobDetail(new JobKey(jobName)));
		return job == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(job);
	}
	
	/**
	 * 
	 * @param jobDetail
	 * @return
	 * @throws SchedulerException
	 */
	JobPayload convertJobPayloadToJobDetail(JobDetail jobDetail) throws SchedulerException {
		if (jobDetail == null) {
			return null;
		}
		
		JobPayload job = new JobPayload();
		job.setDescription(jobDetail.getDescription());
		job.setJobName(jobDetail.getKey().getName());
				
		Trigger trigger = this.scheduler.getTrigger(new TriggerKey("tr_" + jobDetail.getKey().getName()));
		job.setNextFireTime(
				DateTimeFormatter.ISO_DATE_TIME.format(trigger.getNextFireTime().toInstant().atZone(ZoneId.systemDefault())));
		
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		if (jobDataMap != null) {
			Arrays.asList(jobDataMap.getKeys()).stream().forEach( key -> {
				Object object = jobDataMap.get(key);
				if (object != null) {
					job.getDatas().put(key, object.toString());
				}
			});
		}
		
		return job;
	}
	
	@RequestMapping(value="/jobs", method={RequestMethod.GET})
	public ResponseEntity<?> get() throws SchedulerException {
		List<JobPayload> jobPayloads = new ArrayList<>();
		final List<String> jobGroupNames = this.scheduler.getJobGroupNames();
		jobGroupNames.stream().forEach(jobGroupName -> {
			try {
				this.scheduler.getJobKeys(GroupMatcher.groupEquals(jobGroupName)).forEach(jobKey -> {
					try {
						JobPayload jobPayload = convertJobPayloadToJobDetail(this.scheduler.getJobDetail(jobKey));
						if (jobPayload != null) {
							jobPayloads.add(jobPayload);
						}
					} catch (SchedulerException e) {
						logger.error("Error while retrieving job {}", jobKey.getName(), e);
						throw new RuntimeException("Error while retrieving job " + jobKey.getName(), e);
						
					}
				});
			} catch (SchedulerException e) {
				logger.error("Error while retrieving jobs with group {}", jobGroupName, e);
				throw new RuntimeException("Error while retrieving jobs with group " + jobGroupName, e);
			}
		});
		
		return ResponseEntity.ok(jobPayloads);
	}
}
