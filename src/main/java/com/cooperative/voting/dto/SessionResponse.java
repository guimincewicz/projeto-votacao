package com.cooperative.voting.dto;

import java.time.Instant;

public record SessionResponse(
        String id,
        String agendaId,
        Instant openedAt,
        Instant closesAt
) {
}
