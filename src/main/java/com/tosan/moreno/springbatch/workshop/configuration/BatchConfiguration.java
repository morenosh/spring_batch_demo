package com.tosan.moreno.springbatch.workshop.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Bean("rest-job")
    public Job restJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("rest-job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(restStep(jobRepository, transactionManager))
                .build();

    }

//    @Bean("scheduledJob")
//    public Job scheduledJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("scheduled-job", jobRepository)
//                .incrementer(new RunIdIncrementer())
//                .start(restStep(jobRepository, transactionManager))
//                .build();
//    }

    @Bean
    public Step restStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("rest-step", jobRepository)
                .tasklet(restTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet restTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println("rest-step ran ...");
            return RepeatStatus.FINISHED;
        };
    }
}
