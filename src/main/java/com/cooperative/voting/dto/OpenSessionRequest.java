package com.cooperative.voting.dto;

import jakarta.validation.constraints.Positive;

public record OpenSessionRequest(
        @Positive Integer durationMinutes
) {
}
