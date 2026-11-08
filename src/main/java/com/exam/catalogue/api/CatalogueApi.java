package com.exam.catalogue.api;

/** Contrat public du module catalogue. */
public interface CatalogueApi {

    ExemplaireInfo getExemplaire(Long exemplaireId);

    boolean ouvrageExiste(Long ouvrageId);

    long compterExemplairesDisponibles(Long ouvrageId);

    /** DISPONIBLE ou RESERVE -> EMPRUNTE. Lève ConflictException si l'exemplaire est déjà emprunté. */
    void marquerEmprunte(Long exemplaireId);

    /** Retenu pour le prochain adhérent de la file d'attente. */
    void marquerReserve(Long exemplaireId);

    void marquerDisponible(Long exemplaireId);
}
