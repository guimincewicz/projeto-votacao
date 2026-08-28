package com.cooperative.voting.repository;

import com.cooperative.voting.model.Agenda;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgendaRepository extends MongoRepository<Agenda, String> {
}
