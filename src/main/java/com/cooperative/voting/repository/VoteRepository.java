package com.cooperative.voting.repository;

import com.cooperative.voting.model.Vote;
import com.cooperative.voting.model.VoteOption;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoteRepository extends MongoRepository<Vote, String> {

    long countByAgendaIdAndVote(String agendaId, VoteOption vote);
}
