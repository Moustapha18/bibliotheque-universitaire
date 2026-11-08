package com.exam.catalogue.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExemplaireRequest(
        @Schema(example = "1") @NotNull Long ouvrageId,
        @Schema(example = "EX-CA-001") @NotBlank @Size(max = 50) String codeBarre) {
}
