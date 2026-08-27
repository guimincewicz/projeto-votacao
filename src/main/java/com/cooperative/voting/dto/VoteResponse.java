package com.cooperative.voting.dto;
import com.cooperative.voting.model.VoteOption;
import java.time.Instant;
public record VoteResponse(String id, String agendaId, String associateId, VoteOption vote, Instant createdAt) { }
