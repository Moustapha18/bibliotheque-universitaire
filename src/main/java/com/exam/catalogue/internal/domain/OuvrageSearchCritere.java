package com.exam.catalogue.internal.domain;

/** Critères de recherche multicritère (tous optionnels). */
public record OuvrageSearchCritere(String titre, String auteur, Long categorieId, Boolean disponible) {
}
