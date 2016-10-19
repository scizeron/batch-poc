package com.example.batch.scheduler;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.quartz.Scheduler;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BatchSchedulerConfiguration {
	
	@Bean
	public Scheduler scheduler(DataSource datasource) throws Exception {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setDataSource(datasource);
		// because flyaway starts afterwards, need to start the scheduler after the db init
		factory.setAutoStartup(false);
		factory.setWaitForJobsToCompleteOnShutdown(true);
		factory.setQuartzProperties(quartzProperties());
		factory.afterPropertiesSet();
		return factory.getObject();
	}
	
	@Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
	
	@Bean(name="restTemplate") RestTemplate restTemplate() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setMaxConnTotal(10);
		httpClientBuilder.setMaxConnPerRoute(10);

		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(1 * 1000);
		requestBuilder = requestBuilder.setConnectionRequestTimeout(1 * 1000);
		
		httpClientBuilder.setDefaultRequestConfig(requestBuilder.build());

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build()));
	}

}
