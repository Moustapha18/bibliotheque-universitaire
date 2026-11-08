package com.exam.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Format d'erreur unique de l'API")
public record ErrorResponse(
        @Schema(example = "404") int code,
        @Schema(example = "Ouvrage introuvable : 42") String message,
        Instant timestamp,
        List<ValidationDetail> details) {

    public record ValidationDetail(
            @Schema(example = "titre") String field,
            @Schema(example = "ne doit pas être vide") String message) {
    }
}
