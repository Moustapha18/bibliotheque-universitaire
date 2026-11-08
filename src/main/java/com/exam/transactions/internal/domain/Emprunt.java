package com.exam.transactions.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Les autres modules sont référencés par identifiant uniquement (pas de relation JPA). */
@Entity
@Table(name = "emprunt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adherentId;

    @Column(nullable = false)
    private Long exemplaireId;

    @Column(nullable = false)
    private LocalDate dateEmprunt;

    @Column(nullable = false)
    private LocalDate dateRetourPrevue;

    private LocalDate dateRetourEffective;

    @Column(nullable = false)
    private int joursRetard;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal penalite = BigDecimal.ZERO;

    public static Emprunt ouvrir(Long adherentId, Long exemplaireId, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        Emprunt emprunt = new Emprunt();
        emprunt.adherentId = adherentId;
        emprunt.exemplaireId = exemplaireId;
        emprunt.dateEmprunt = dateEmprunt;
        emprunt.dateRetourPrevue = dateRetourPrevue;
        return emprunt;
    }

    public boolean isEnCours() {
        return dateRetourEffective == null;
    }

    public void cloturer(LocalDate dateRetour, int joursRetard, BigDecimal penalite) {
        this.dateRetourEffective = dateRetour;
        this.joursRetard = joursRetard;
        this.penalite = penalite;
    }
}
