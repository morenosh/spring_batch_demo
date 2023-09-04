package com.tosan.moreno.springbatch.workshop.persist.repo;

import com.tosan.moreno.springbatch.workshop.persist.entity.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PersonRepository extends CrudRepository<Person, Long> {
    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);
}
