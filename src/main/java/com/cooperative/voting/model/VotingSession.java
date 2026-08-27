package com.cooperative.voting.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("voting_sessions")
public class VotingSession {
    @Id private String id;
    @Indexed(unique = true) private String agendaId;
    private Instant openedAt;
    private Instant closesAt;
    protected VotingSession() { }
    public VotingSession(String agendaId, Instant openedAt, Instant closesAt) { this.agendaId = agendaId; this.openedAt = openedAt; this.closesAt = closesAt; }
    public String getId() { return id; } public String getAgendaId() { return agendaId; } public Instant getOpenedAt() { return openedAt; } public Instant getClosesAt() { return closesAt; }
}
