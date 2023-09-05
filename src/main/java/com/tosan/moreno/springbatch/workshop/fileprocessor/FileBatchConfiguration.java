package com.tosan.moreno.springbatch.workshop.fileprocessor;

import com.tosan.moreno.springbatch.workshop.dto.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Random;

/**
 * @author P.khoshkhou
 * @since 8/30/2023
 */
@Configuration
public class FileBatchConfiguration {

    @Bean
    public Job csvFileJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("csvFileJob", jobRepository)
//                .incrementer(new RunIdIncrementer())
                .start(addressStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step addressStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("addressStep", jobRepository)
                .<Person, Person>chunk(2, transactionManager)
                .reader(reader())
                .processor(addAddress())
                .writer(writer())
                .build();
    }

    @Bean
    public ItemProcessor<? super Person, ? extends Person> addAddress() {
        return (ItemProcessor<Person, Person>) item -> {
            item.setAddress("work at tosan");
            item.setPhoneNumber("98912125" + new Random().nextInt(100, 200));
            if(item.getFirstName().equals("marjaneh")){
                throw new RuntimeException("Error");
            }
            return item;
        };
    }

    @Bean
    public FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("sample-data.csv")); // Set the input file name
        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("firstName", "lastName");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean
    public FlatFileItemWriter<Person> writer() {
        FlatFileItemWriter<Person> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("output-data.csv"));
        writer.setLineAggregator(new DelimitedLineAggregator<>() {{
            setDelimiter(",");
            setFieldExtractor(new BeanWrapperFieldExtractor<>() {{
                setNames(new String[]{"firstName", "lastName", "address", "phoneNumber"});
            }});
        }});
        return writer;
    }
}
