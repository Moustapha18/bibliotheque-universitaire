package com.exam.acteurs.internal.dto;

import com.exam.acteurs.api.TypeAdherent;

public record AdherentResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        TypeAdherent type,
        int quotaMax,
        boolean actif) {
}
