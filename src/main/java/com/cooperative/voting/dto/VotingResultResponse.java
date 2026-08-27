package com.cooperative.voting.dto;
public record VotingResultResponse(String agendaId, long yesVotes, long noVotes, long totalVotes, String result) { }
