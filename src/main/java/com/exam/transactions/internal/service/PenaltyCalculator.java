package com.exam.transactions.internal.service;

import com.exam.transactions.internal.config.LibraryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Règles de durée de prêt, de retard et de pénalité (logique pure, sans accès base). */
@Component
@RequiredArgsConstructor
public class PenaltyCalculator {

    private final LibraryProperties properties;

    public LocalDate dueDate(LocalDate borrowDate) {
        return borrowDate.plusDays(properties.loanDays());
    }

    public int lateDays(LocalDate dueDate, LocalDate returnDate) {
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
    }

    public BigDecimal penalty(int lateDays) {
        return properties.penaltyPerDay()
                .multiply(BigDecimal.valueOf(lateDays))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
