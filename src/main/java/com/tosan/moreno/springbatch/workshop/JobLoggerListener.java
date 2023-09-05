package com.tosan.moreno.springbatch.workshop;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * @author P.khoshkhou
 * @since 8/29/2023
 */
public class JobLoggerListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("job started");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("job finished");
    }
}
