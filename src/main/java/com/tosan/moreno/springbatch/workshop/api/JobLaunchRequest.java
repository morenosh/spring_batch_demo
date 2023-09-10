package com.tosan.moreno.springbatch.workshop.api;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.Map;

public record JobLaunchRequest(String jobName, Map<String, String> parameters) {

    public JobParameters jobParameters() {
        JobParametersBuilder builder = new JobParametersBuilder();
        parameters.forEach(builder::addString);
        return builder.toJobParameters();
    }
}
