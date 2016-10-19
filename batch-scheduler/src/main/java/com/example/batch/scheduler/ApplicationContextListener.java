package com.example.batch.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextListener {

	@Autowired
	private Scheduler scheduler;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        try {
			if (! this.scheduler.isStarted()) {
				this.scheduler.start();
			}
		} catch (SchedulerException e) {
			logger.error("Impossible to start the scheduler", e);
		}
    }
}
