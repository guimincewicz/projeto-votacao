package com.cooperative.voting.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAgendaRequest(
        @NotBlank String title,
        @NotBlank String description
) {
}
