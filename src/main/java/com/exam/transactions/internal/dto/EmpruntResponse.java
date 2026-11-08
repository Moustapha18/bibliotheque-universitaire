package com.exam.transactions.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmpruntResponse(
        Long id,
        Long adherentId,
        Long exemplaireId,
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourEffective,
        int joursRetard,
        BigDecimal penalite) {
}
