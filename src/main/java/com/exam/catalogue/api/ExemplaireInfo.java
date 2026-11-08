package com.exam.catalogue.api;

/** Vue publique d'un exemplaire, destinée aux autres modules. */
public record ExemplaireInfo(Long id, Long ouvrageId, StatutExemplaire statut) {
}
