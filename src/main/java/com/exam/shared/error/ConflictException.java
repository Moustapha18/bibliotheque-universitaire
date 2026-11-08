package com.exam.shared.error;

/** Violation d'une règle de gestion (quota, exemplaire indisponible, doublon...). Traduit en HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
