package com.exam.transactions.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;

/** Paramètres de prêt, modifiables par configuration (library.loan-days, library.penalty-per-day). */
@ConfigurationProperties(prefix = "library")
public record LibraryProperties(
        @DefaultValue("14") int loanDays,
        @DefaultValue("0.50") BigDecimal penaltyPerDay) {
}
