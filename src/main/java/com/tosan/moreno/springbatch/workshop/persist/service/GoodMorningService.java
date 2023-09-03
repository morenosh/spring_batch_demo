package com.tosan.moreno.springbatch.workshop.persist.service;

import com.tosan.moreno.springbatch.workshop.persist.entity.GoodMorning;
import com.tosan.moreno.springbatch.workshop.persist.repo.GoodMorningRepository;
import com.tosan.moreno.springbatch.workshop.persist.repo.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class GoodMorningService {

    private final GoodMorningRepository goodMorningRepository;
    private final PersonRepository personRepository;

    public GoodMorningService(GoodMorningRepository goodMorningRepository, PersonRepository personRepository) {
        this.goodMorningRepository = goodMorningRepository;
        this.personRepository = personRepository;
    }

    public GoodMorning save(GoodMorning goodMorning) {
        personRepository.findByFirstNameAndLastName(goodMorning.getPerson().getFirstName(), goodMorning.getPerson().getLastName()).ifPresent(
                goodMorning::setPerson);
        if (goodMorning.getPerson().getId() != null && goodMorningRepository.existsByDateAndPerson(goodMorning.getDate(), goodMorning.getPerson())) {
            return null;
        }
        return goodMorningRepository.save(goodMorning);
    }
}
