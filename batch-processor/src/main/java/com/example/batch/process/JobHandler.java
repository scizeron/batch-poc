package com.example.batch.process;

import com.example.batch.process.v1.JobEvent;

/**
 * 
 * @author 212552868
 *
 */
public interface JobHandler {

	void handleJob(JobEvent jobEvent);

}