package com.tosan.moreno.springbatch.workshop.controller;

import com.tosan.moreno.springbatch.workshop.api.JobLaunchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchController {

    private final ApplicationContext context;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    @PostMapping("/run")
    public ExitStatus runRestJob(@RequestBody JobLaunchRequest jobLaunchRequest) throws
            JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {
        Job job = context.getBean(jobLaunchRequest.jobName(), Job.class);
        JobParameters jobParameters = new JobParametersBuilder(jobLaunchRequest.jobParameters(), jobExplorer)
                .getNextJobParameters(job)
                .toJobParameters();
        return jobLauncher.run(job, jobParameters).getExitStatus();
    }
}
