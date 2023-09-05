package com.tosan.moreno.springbatch.workshop;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.CompositeStepExecutionListener;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.Random;

@SpringBootApplication
public class WorkshopApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkshopApplication.class, "executionDate=2023/11/30", "name=Sina,java.lang.String,true");
    }

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("job10", jobRepository)
                .start(step3(jobRepository, transactionManager))
                .next(step4(jobRepository, transactionManager))
                .validator(validator())
                .incrementer(new DailyJobTimestamper())
                .listener(new JobLoggerListener())
                .build();
    }

    @Bean
    public Step step3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step3", jobRepository).tasklet(tasklet(null), transactionManager)
                .listener(compositeStepExecutionListener())
                .build();
    }

    @Bean
    public Step step4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step4", jobRepository).tasklet(tasklet(null), transactionManager).build();
    }

    @StepScope
    @Bean
    public Tasklet tasklet(@Value("#{jobParameters['name']}") String name) {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                String nameFromContext = (String) chunkContext.getStepContext().getJobParameters().get("name");
                System.out.println("name from context is : " + nameFromContext);
                System.out.println("name from @value is : " + name);
                ExecutionContext jobContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
                jobContext.put("mustbe", "here it is");
                return new Random().nextBoolean() ? RepeatStatus.CONTINUABLE : RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    public JobParametersValidator requiredValidator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{"executionDate"});
        validator.setOptionalKeys(new String[]{"name", "currentDate"});
        return validator;
    }

    @Bean
    public CompositeJobParametersValidator validator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(new ParameterValidator(), requiredValidator()));
        return validator;
    }

    @Bean
    public StepExecutionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"mustbe"});
        listener.setStrict(true);
        return listener;
    }

    @Bean
    public CompositeStepExecutionListener compositeStepExecutionListener() {
        CompositeStepExecutionListener compositeStepExecutionListener = new CompositeStepExecutionListener();
        compositeStepExecutionListener.register(promotionListener());
        return compositeStepExecutionListener;
    }

}
