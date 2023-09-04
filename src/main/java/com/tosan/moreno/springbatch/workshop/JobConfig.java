package com.tosan.moreno.springbatch.workshop;

import com.tosan.moreno.springbatch.workshop.dto.PersonGoodMorningDto;
import com.tosan.moreno.springbatch.workshop.persist.entity.GoodMorning;
import com.tosan.moreno.springbatch.workshop.persist.entity.Person;
import com.tosan.moreno.springbatch.workshop.persist.service.GoodMorningService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.MalformedURLException;


@Configuration
public class JobConfig {

    private final PlatformTransactionManager transactionManager;
    private final GoodMorningService goodMorningService;
    private final JobRepository customJobRepository;

    public JobConfig(JobRepository customJobRepository, PlatformTransactionManager transactionManager, GoodMorningService goodMorningService) {
        this.customJobRepository = customJobRepository;
        this.transactionManager = transactionManager;
        this.goodMorningService = goodMorningService;
    }

    @Bean
    public Job firstJob() {
        return new JobBuilder("firstJob", customJobRepository)
                .start(firstStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step firstStep() {
        return new StepBuilder("firstStep", customJobRepository)
                .<PersonGoodMorningDto, GoodMorning>chunk(2, transactionManager)
                .reader(csvReader(null))
                .processor(assembler())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PersonGoodMorningDto> csvReader(@Value("#{jobParameters['fileAddress']}") String fileAddress) {

        FlatFileItemReader<PersonGoodMorningDto> reader = new FlatFileItemReader<>();
        try {
            reader.setResource(new UrlResource("file:///" + fileAddress));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        reader.setLinesToSkip(1);
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("date", "firstName", "lastName", "clause");
        lineTokenizer.setDelimiter(",");
        DefaultLineMapper<PersonGoodMorningDto> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(new RecordFieldSetMapper<>(PersonGoodMorningDto.class));
        reader.setLineMapper(lineMapper);
        return reader;
    }

    private ItemProcessor<? super PersonGoodMorningDto, ? extends GoodMorning> assembler() {
        return (ItemProcessor<PersonGoodMorningDto, GoodMorning>) item -> {
            Person p = new Person();
            p.setFirstName(item.firstName());
            p.setLastName(item.lastName());
            GoodMorning g = new GoodMorning();
            g.setDate(item.date());
            g.setGoodMorningClause(item.clause());
            g.setPerson(p);
            return g;
        };
    }

    private ItemWriter<? super GoodMorning> writer() {
        return (ItemWriter<GoodMorning>) chunk -> chunk.forEach(goodMorningService::save);
    }
}
