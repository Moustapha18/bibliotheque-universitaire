package com.exam.catalogue.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategorieRequest(
        @Schema(example = "Informatique") @NotBlank @Size(max = 100) String nom,
        @Schema(example = "Programmation, génie logiciel, bases de données") @Size(max = 255) String description) {
}
