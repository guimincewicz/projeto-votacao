package com.cooperative.voting.repository;

import com.cooperative.voting.model.VotingSession;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VotingSessionRepository extends MongoRepository<VotingSession, String> {

    Optional<VotingSession> findByAgendaId(String agendaId);
}
