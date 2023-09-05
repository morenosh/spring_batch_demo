package com.tosan.moreno.springbatch.workshop;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author P.khoshkhou
 * @since 8/28/2023
 */
@Configuration
public class JobConfiguration {

    @Setter(onMethod = @__(@Autowired))
    private JobRegistry jobRegistry;

    @SneakyThrows
    @Bean
    public Object setUpJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        jobRegistry.register(new ReferenceJobFactory(new JobBuilder("job89", jobRepository)
                .start(step2(jobRepository, transactionManager)).build()));
        return new Object();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepInRegistry", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        String name = (String) chunkContext.getStepContext().getJobParameters().get("name");
                        System.out.println("name is : " + name);
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager).build();
    }
}
