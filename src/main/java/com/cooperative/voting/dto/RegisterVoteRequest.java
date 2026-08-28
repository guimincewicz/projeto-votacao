package com.cooperative.voting.dto;

import com.cooperative.voting.model.VoteOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterVoteRequest(
        @NotBlank String associateId,
        @NotBlank String cpf,
        @NotNull VoteOption vote
) {
}
