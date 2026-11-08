package com.exam.acteurs.internal.dto;

import com.exam.acteurs.api.TypeAdherent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdherentRequest(
        @Schema(example = "Martin") @NotBlank @Size(max = 100) String nom,
        @Schema(example = "Alice") @NotBlank @Size(max = 100) String prenom,
        @Schema(example = "alice.martin@univ.sn") @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "ETUDIANT") @NotNull TypeAdherent type) {
}
