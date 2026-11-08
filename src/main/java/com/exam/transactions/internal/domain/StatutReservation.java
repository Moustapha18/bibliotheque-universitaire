package com.exam.transactions.internal.domain;

public enum StatutReservation {
    /** Dans la file d'attente, aucun exemplaire encore attribué. */
    EN_ATTENTE,
    /** Un exemplaire est mis de côté pour l'adhérent. */
    PRETE,
    /** L'adhérent a emprunté l'exemplaire réservé. */
    HONOREE,
    ANNULEE
}
