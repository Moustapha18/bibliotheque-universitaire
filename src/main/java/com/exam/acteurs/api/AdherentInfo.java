package com.exam.acteurs.api;

/** Vue publique d'un adhérent, destinée aux autres modules. */
public record AdherentInfo(Long id, String nomComplet, TypeAdherent type, int quotaMax, boolean actif) {
}
