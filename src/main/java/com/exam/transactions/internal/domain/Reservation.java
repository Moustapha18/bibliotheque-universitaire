package com.exam.transactions.internal.domain;

import com.exam.shared.error.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ouvrageId;

    @Column(nullable = false)
    private Long adherentId;

    /** Renseigné quand la réservation passe à PRETE. */
    private Long exemplaireId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutReservation statut;

    @Column(nullable = false)
    private Instant dateReservation;

    public static Reservation creer(Long ouvrageId, Long adherentId, Instant dateReservation) {
        Reservation reservation = new Reservation();
        reservation.ouvrageId = ouvrageId;
        reservation.adherentId = adherentId;
        reservation.dateReservation = dateReservation;
        reservation.statut = StatutReservation.EN_ATTENTE;
        return reservation;
    }

    public boolean estPrete() {
        return statut == StatutReservation.PRETE;
    }

    public void marquerPrete(Long exemplaireId) {
        if (statut != StatutReservation.EN_ATTENTE) {
            throw new ConflictException("La réservation %d n'est pas en attente".formatted(id));
        }
        this.exemplaireId = exemplaireId;
        this.statut = StatutReservation.PRETE;
    }

    public void honorer() {
        if (!estPrete()) {
            throw new ConflictException("La réservation %d n'est pas prête".formatted(id));
        }
        this.statut = StatutReservation.HONOREE;
    }

    public void annuler() {
        if (statut == StatutReservation.HONOREE || statut == StatutReservation.ANNULEE) {
            throw new ConflictException("La réservation %d est déjà %s".formatted(id, statut));
        }
        this.statut = StatutReservation.ANNULEE;
    }
}
