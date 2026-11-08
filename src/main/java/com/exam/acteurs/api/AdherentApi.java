package com.exam.acteurs.api;

/** Contrat public du module acteurs. */
public interface AdherentApi {

    /** @throws com.exam.shared.error.ResourceNotFoundException si l'adhérent n'existe pas */
    AdherentInfo getAdherent(Long adherentId);
}
