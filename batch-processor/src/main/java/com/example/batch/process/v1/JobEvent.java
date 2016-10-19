package com.example.batch.process.v1;

import java.util.Map;

public class JobEvent {

	private String jobName;
	
	private Map<String,String> jobParameters;
	
	public JobEvent(String jobName, Map<String,String> jobParameters) {
		this.jobParameters = jobParameters;
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Map<String, String> getJobParameters() {
		return jobParameters;
	}

	public void setJobParameters(Map<String, String> jobParameters) {
		this.jobParameters = jobParameters;
	}



}
