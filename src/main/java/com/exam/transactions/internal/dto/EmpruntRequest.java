package com.exam.transactions.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EmpruntRequest(
        @Schema(example = "1") @NotNull Long adherentId,
        @Schema(example = "3") @NotNull Long exemplaireId) {
}
