package com.tosan.moreno.springbatch.workshop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private Date timestamp;
    private double amount;
    @ManyToOne
    private AccountSummary accountSummary;
}
