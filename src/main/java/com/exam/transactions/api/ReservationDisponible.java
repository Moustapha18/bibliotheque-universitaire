package com.exam.transactions.api;

/** Événement publié quand un exemplaire est mis de côté pour le premier de la file d'attente. */
public record ReservationDisponible(Long reservationId, Long adherentId, Long ouvrageId, Long exemplaireId) {
}
