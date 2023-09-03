package com.tosan.moreno.springbatch.workshop.dto;


import java.util.Date;

public record PersonGoodMorningDto(
        Date date,
        String firstName,
        String lastName,
        String clause) {
}

