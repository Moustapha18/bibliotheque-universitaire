package com.exam.transactions.api;

import java.time.LocalDate;

/** Événement publié quand un exemplaire vient d'être emprunté. */
public record EmpruntEffectue(Long empruntId, Long adherentId, Long exemplaireId, LocalDate dateRetourPrevue) {
}
