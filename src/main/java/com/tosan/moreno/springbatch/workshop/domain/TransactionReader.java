package com.tosan.moreno.springbatch.workshop.domain;

import com.tosan.moreno.springbatch.workshop.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.transform.FieldSet;

@RequiredArgsConstructor
public class TransactionReader implements ItemStreamReader<Transaction> {

    private final ItemStreamReader<FieldSet> fieldSetReader;
    private StepExecution stepExecution;
    private int itemCount = 0;
    private int expectedRecordCount = 0;

    @Override
    public Transaction read() throws Exception {
        return process(fieldSetReader.read());
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        fieldSetReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        fieldSetReader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        fieldSetReader.close();
    }

    private Transaction process(FieldSet fieldSet) {
        Transaction result = null;

        if (fieldSet != null) {
            if (fieldSet.getFieldCount() > 1) {
                result = new Transaction();
                result.setAccountNumber(fieldSet.readString(0));
                result.setTimestamp(fieldSet.readDate(1, "yyyy-MM-dd'T'HH:mm:ss'Z'"));
                result.setAmount(fieldSet.readDouble(2));

                itemCount++;
            } else {
                expectedRecordCount = fieldSet.readInt(0);

                if (expectedRecordCount != itemCount) {
                    System.out.println("Stopped duo to wrong item counts");
                    stepExecution.setTerminateOnly();
                }
            }
        }
        return result;
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (itemCount == expectedRecordCount) {
            return stepExecution.getExitStatus();
        } else {
            return ExitStatus.STOPPED;
        }
    }

    @BeforeStep
    public void beforeStep(StepExecution execution) {
        this.stepExecution = execution;
    }

}
