package com.cooperative.voting.dto;

import java.time.Instant;

public record AgendaResponse(
        String id,
        String title,
        String description,
        Instant createdAt
) {
}
