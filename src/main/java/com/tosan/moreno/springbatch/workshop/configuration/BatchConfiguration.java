package com.tosan.moreno.springbatch.workshop.configuration;

import com.tosan.moreno.springbatch.workshop.domain.TransactionApplierProcessor;
import com.tosan.moreno.springbatch.workshop.domain.TransactionReader;
import com.tosan.moreno.springbatch.workshop.entity.AccountSummary;
import com.tosan.moreno.springbatch.workshop.entity.Transaction;
import com.tosan.moreno.springbatch.workshop.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    @Bean
    @StepScope
    public FlatFileItemReader<FieldSet> fileItemReader(
            @Value("#{jobParameters['transactionFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<FieldSet>()
                .name("fileItemReader")
                .resource(inputFile)
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
    }

    @Bean
    @StepScope
    public TransactionReader transactionReader() {
        return new TransactionReader(fileItemReader(null));
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("""
                        INSERT INTO TRANSACTION(ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT)
                        VALUES((SELECT a.ID FROM ACCOUNT_SUMMARY a WHERE ACCOUNT_NUMBER = :accountNumber),
                        :timestamp, :amount)""")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step importTransactionFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("importTransactionFileStep", jobRepository)
                .<Transaction, Transaction>chunk(10, transactionManager)
                .reader(transactionReader())
                .writer(transactionWriter(null))
                .allowStartIfComplete(true)
                .listener(transactionReader())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<AccountSummary> accountSummaryReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AccountSummary>()
                .name("accountSummaryReader")
                .dataSource(dataSource)
                .sql("""
                        SELECT ACCOUNT_NUMBER, CURRENT_BALANCE
                        FROM ACCOUNT_SUMMARY A
                        WHERE A.ID IN (
                        SELECT DISTINCT T.ACCOUNT_SUMMARY_ID
                        FROM TRANSACTION T)
                        ORDER BY A.ACCOUNT_NUMBER""")
                .rowMapper((rs, rowNum) -> {
                    AccountSummary accountSummary = new AccountSummary();
                    accountSummary.setAccountNumber(rs.getString("account_number"));
                    accountSummary.setCurrentBalance(rs.getDouble("current_balance"));
                    return accountSummary;
                })
                .build();
    }

    @Bean
    public TransactionApplierProcessor transactionApplierProcessor(TransactionRepository repository) {
        return new TransactionApplierProcessor(repository);
    }

    @Bean
    public JdbcBatchItemWriter<AccountSummary> accountSummaryItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AccountSummary>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("""
                        UPDATE ACCOUNT_SUMMARY
                        SET CURRENT_BALANCE = :currentBalance
                        WHERE ACCOUNT_NUMBER = :accountNumber
                        """)
                .build();
    }

    @Bean
    public Step applyTransactionStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("applyTransactionStep", jobRepository)
                .<AccountSummary, AccountSummary>chunk(10, transactionManager)
                .reader(accountSummaryReader(null))
                .processor(transactionApplierProcessor(null))
                .writer(accountSummaryItemWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<AccountSummary> accountSummaryFileWriter(
            @Value("#{jobParameters['summaryFile']}") WritableResource summaryFile) {
        DelimitedLineAggregator<AccountSummary> lineAggregator = new DelimitedLineAggregator<>();
        BeanWrapperFieldExtractor<AccountSummary> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"accountNumber", "currentBalance"});
        fieldExtractor.afterPropertiesSet();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return new FlatFileItemWriterBuilder<AccountSummary>()
                .name("accountSummaryFileWriter")
                .resource(summaryFile)
                .lineAggregator(lineAggregator)
                .build();
    }

    @Bean
    public Step generateAccountSummaryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("generateAccountSummaryStep", jobRepository)
                .<AccountSummary, AccountSummary>chunk(10, transactionManager)
                .reader(accountSummaryReader(null))
                .writer(accountSummaryFileWriter(null))
                .build();
    }

    @Bean
    public Job transactionJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("transactionJob", jobRepository)
                .start(importTransactionFileStep(jobRepository, transactionManager))
                .on("STOPPED").stopAndRestart(importTransactionFileStep(jobRepository, transactionManager))
                .from(importTransactionFileStep(jobRepository, transactionManager))
                .on("*").to(applyTransactionStep(jobRepository, transactionManager))
                .from(applyTransactionStep(jobRepository, transactionManager))
                .next(generateAccountSummaryStep(jobRepository, transactionManager))
                .end()
                .build();
    }
}
