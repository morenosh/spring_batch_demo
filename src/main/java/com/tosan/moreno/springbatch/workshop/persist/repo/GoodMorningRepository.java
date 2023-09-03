package com.tosan.moreno.springbatch.workshop.persist.repo;

import com.tosan.moreno.springbatch.workshop.persist.entity.GoodMorning;
import com.tosan.moreno.springbatch.workshop.persist.entity.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;

public interface GoodMorningRepository extends CrudRepository<GoodMorning, Long> {
    boolean existsByDateAndPerson(Date date, Person person);
}
