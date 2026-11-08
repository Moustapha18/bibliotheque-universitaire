package com.exam.transactions.api;

import java.math.BigDecimal;

/** Événement publié quand un exemplaire vient d'être retourné (joursRetard = 0 si rendu à temps). */
public record RetourEffectue(Long empruntId, Long adherentId, Long exemplaireId, int joursRetard, BigDecimal penalite) {
}
