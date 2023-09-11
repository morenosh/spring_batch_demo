package com.tosan.moreno.springbatch.workshop.domain;

import com.tosan.moreno.springbatch.workshop.entity.AccountSummary;
import com.tosan.moreno.springbatch.workshop.entity.Transaction;
import com.tosan.moreno.springbatch.workshop.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

@RequiredArgsConstructor
public class TransactionApplierProcessor implements ItemProcessor<AccountSummary, AccountSummary> {

    private final TransactionRepository repository;

    @Override
    public AccountSummary process(AccountSummary accountSummary) {
        List<Transaction> transactions = repository.findByAccountNumber(accountSummary.getAccountNumber());
        transactions.forEach(transaction ->
                accountSummary.setCurrentBalance(accountSummary.getCurrentBalance() + transaction.getAmount()));
        return accountSummary;
    }
}
