package com.tosan.moreno.springbatch.workshop;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

/**
 * @author P.khoshkhou
 * @since 8/28/2023
 */
public class ParameterValidator implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String name = parameters.getString("name");
        if(!StringUtils.hasText(name)) {
            throw new JobParametersInvalidException("name parameter is missing");
        }
        else if(!StringUtils.startsWithIgnoreCase(name, "sina")) {
            throw new JobParametersInvalidException("name parameter does " +
                    "not end with sina");
        }
    }
}
