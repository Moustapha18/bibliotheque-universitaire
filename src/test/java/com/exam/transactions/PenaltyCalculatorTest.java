package com.exam.transactions;

import com.exam.transactions.internal.config.LibraryProperties;
import com.exam.transactions.internal.service.PenaltyCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PenaltyCalculatorTest {

    private final PenaltyCalculator calculator =
            new PenaltyCalculator(new LibraryProperties(14, new BigDecimal("0.50")));

    @Test
    void dueDateIsLoanDurationAfterBorrowDate() {
        assertThat(calculator.dueDate(LocalDate.of(2026, 11, 1))).isEqualTo(LocalDate.of(2026, 11, 15));
    }

    @Test
    void returnedOnDueDateHasNoLateDay() {
        LocalDate due = LocalDate.of(2026, 11, 15);
        assertThat(calculator.lateDays(due, due)).isZero();
    }

    @Test
    void returnedEarlyHasNoLateDay() {
        assertThat(calculator.lateDays(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 10))).isZero();
    }

    @Test
    void returnedFiveDaysAfterDueDateIsFiveLateDays() {
        assertThat(calculator.lateDays(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 20))).isEqualTo(5);
    }

    @Test
    void penaltyIsLateDaysTimesDailyRate() {
        assertThat(calculator.penalty(5)).isEqualByComparingTo("2.50");
    }

    @Test
    void noLateDayMeansNoPenalty() {
        assertThat(calculator.penalty(0)).isEqualByComparingTo("0.00");
    }
}
