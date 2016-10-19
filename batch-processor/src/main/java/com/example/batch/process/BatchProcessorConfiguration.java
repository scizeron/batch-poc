package com.example.batch.process;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.process.data.DbRecord;
import com.example.batch.process.data.Mapping;
import com.example.batch.process.item.FakeDbItemReader;
import com.example.batch.process.item.FakeDbItemWriter;
import com.example.batch.process.item.FakeMappingItemReader;
import com.example.batch.process.item.MappingItemWriter;

@Configuration
@EnableBatchProcessing
@EnableAsync
public class BatchProcessorConfiguration  {

	@Bean
	@Primary
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource batchDataSource() {
	    return DataSourceBuilder.create().build();
	}
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
	@Bean
	public JobRepository getJobRepository(@Qualifier("batchDataSource") DataSource dataSource) throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(this.transactionManager);
		factory.afterPropertiesSet();
		return factory.getObject();
	}
	
	@Bean
	public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}
	
	/**
	 * In order to have distinct jb names ... Link betweeb scheduled jobs and their executions
	 */
	public Job getExtractJob(String jobName) {
		return this.jobBuilderFactory.get(String.format("extractJob-%s", jobName))
				.flow(mappingStep())
				.next(extractionStep())
				.end()
				.build();
	}
	
	@Bean
    Step mappingStep() {
		return this.stepBuilderFactory.get("loadMapping")
				.<Mapping,Mapping>chunk(1)
				.reader(mappingItemReader())
				.writer(mappingItemWriter())
				.build();
	}
	
	@Bean
	Step extractionStep() {
		return this.stepBuilderFactory.get("extraction")
				.<DbRecord,DbRecord>chunk(50)
				.reader(extractionReader())
				.writer(extractionWriter())
				.build();
	}
	
	/**
	 * @Bean
	 * @StepScope
	 * public MyInterface myBean() {
	 * 	return new MyInterfaceImpl();
	 * }
	 * 
	 * You are telling Spring to use the proxy mode ScopedProxyMode.TARGET_CLASS. 
	 * However, by returning the MyInterface, instead of the MyInterfaceImpl, 
	 * the proxy only has visibility into the methods on the MyInterface. 
	 * 
	 * This prevents Spring Batch from being able to find the methods on MyInterfaceImpl 
	 * that have been annotated with the listener annotations like @BeforeStep. 
	 * 
	 * The correct way to configure this is to return MyInterfaceImpl 
	 * on your configuration method like below:
	 * 
	 * @Bean
	 * @StepScope
	 * public MyInterfaceImpl myBean() {
	 * 	return new MyInterfaceImpl();
	 * }
	 */
	
	@Bean 
	@StepScope
	FakeMappingItemReader mappingItemReader() {
		return new FakeMappingItemReader();
	}
	
	@Bean
	@StepScope
	MappingItemWriter mappingItemWriter() {
		return new MappingItemWriter();
	}
		
	@Bean 
	@StepScope
	FakeDbItemReader extractionReader() {
		return new FakeDbItemReader();
	}
	
	@Bean
	@StepScope
	FakeDbItemWriter extractionWriter() {
		return new FakeDbItemWriter();
	}
} 
