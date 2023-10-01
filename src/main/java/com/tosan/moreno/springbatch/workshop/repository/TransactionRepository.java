package com.tosan.moreno.springbatch.workshop.repository;

import com.tosan.moreno.springbatch.workshop.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from Transaction t inner join AccountSummary a on " +
           "t.accountSummary.id = a.id where a.accountNumber = :accountNumber")
    List<Transaction> findByAccountNumber(String accountNumber);
}
