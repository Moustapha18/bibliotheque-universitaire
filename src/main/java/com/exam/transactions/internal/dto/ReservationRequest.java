package com.exam.transactions.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @Schema(example = "2") @NotNull Long ouvrageId,
        @Schema(example = "3") @NotNull Long adherentId) {
}
