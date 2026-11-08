package com.exam.catalogue.internal.dto;

public record OuvrageResponse(
        Long id,
        String titre,
        String auteur,
        String isbn,
        Integer anneePublication,
        Long categorieId,
        String categorieNom) {
}
