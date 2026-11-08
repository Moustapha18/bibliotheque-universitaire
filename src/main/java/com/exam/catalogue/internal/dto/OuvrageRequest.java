package com.exam.catalogue.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OuvrageRequest(
        @Schema(example = "Clean Architecture") @NotBlank @Size(max = 255) String titre,
        @Schema(example = "Robert C. Martin") @NotBlank @Size(max = 255) String auteur,
        @Schema(description = "ISBN-10 ou ISBN-13, sans tirets", example = "9780134494166")
        @NotBlank @Pattern(regexp = "^(\\d{9}[\\dX]|\\d{13})$", message = "ISBN invalide (10 ou 13 caractères, sans tirets)")
        String isbn,
        @Schema(example = "2017") @Min(1450) @Max(2100) Integer anneePublication,
        @Schema(example = "1") @NotNull Long categorieId) {
}
