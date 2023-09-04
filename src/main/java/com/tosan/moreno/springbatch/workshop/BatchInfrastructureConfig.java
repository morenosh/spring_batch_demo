package com.tosan.moreno.springbatch.workshop;

import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchInfrastructureConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.batch")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @BatchDataSource
    public DataSource batchDataSource(
            @Qualifier("batchDataSourceProperties") DataSourceProperties batchDataSourceProperties) {
        return batchDataSourceProperties.initializeDataSourceBuilder().build();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //// we will need lines bellow if @BatchDataSource not added above "batchDataSource" bean
//    @Bean
//    @Primary
//    public BatchDataSourceScriptDatabaseInitializer customBatchScriptDatabaseInitializer(@Qualifier("batchDataSource") DataSource secondDataSource,
//                                                                                         BatchProperties properties) {
//        return new BatchDataSourceScriptDatabaseInitializer(secondDataSource, properties.getJdbc());
//    }
//
//    @Bean
//    @Primary
//    public JobRepository customJobRepository(@Qualifier("batchDataSource") DataSource batchDataSource, PlatformTransactionManager transactionManager)
//            throws Exception {
//        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//        factory.setDataSource(batchDataSource);
//        factory.setTransactionManager(transactionManager);
//        factory.afterPropertiesSet();
//        return factory.getObject();
//    }
//
//    @Bean
//    @Primary
//    public JobExplorer customJobExplorer(@Qualifier("batchDataSource") DataSource batchDataSource, PlatformTransactionManager transactionManager) {
//        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
//        jobExplorerFactoryBean.setDataSource(batchDataSource);
//        jobExplorerFactoryBean.setTransactionManager(transactionManager);
//        jobExplorerFactoryBean.setJdbcOperations(new JdbcTemplate(batchDataSource));
//        try {
//            jobExplorerFactoryBean.afterPropertiesSet();
//            return jobExplorerFactoryBean.getObject();
//        } catch (Exception e) {
//            throw new BatchConfigurationException("Unable to configure the default job explorer", e);
//        }
//    }
//
//    @Bean
//    @Primary
//    public JobLauncher customJobLauncher(@Qualifier("customJobRepository") JobRepository jobRepository) throws Exception {
//        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
//        jobLauncher.setJobRepository(jobRepository);
//        jobLauncher.afterPropertiesSet();
//        return jobLauncher;
//    }
}
