package com.cooperative.voting.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("votes")
@CompoundIndex(name = "agenda_associate_unique", def = "{'agendaId': 1, 'associateId': 1}", unique = true)
public class Vote {
    @Id private String id;
    @Indexed private String agendaId;
    private String associateId;
    private VoteOption vote;
    private Instant createdAt;
    protected Vote() { }
    public Vote(String agendaId, String associateId, VoteOption vote, Instant createdAt) { this.agendaId = agendaId; this.associateId = associateId; this.vote = vote; this.createdAt = createdAt; }
    public String getId() { return id; } public String getAgendaId() { return agendaId; } public String getAssociateId() { return associateId; } public VoteOption getVote() { return vote; } public Instant getCreatedAt() { return createdAt; }
}
