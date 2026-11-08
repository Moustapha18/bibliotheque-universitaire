package com.exam.transactions.internal.dto;

import com.exam.transactions.internal.domain.StatutReservation;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long ouvrageId,
        Long adherentId,
        Long exemplaireId,
        StatutReservation statut,
        Instant dateReservation) {
}
