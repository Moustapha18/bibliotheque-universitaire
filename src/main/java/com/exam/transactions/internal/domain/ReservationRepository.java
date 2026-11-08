package com.exam.transactions.internal.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Tête de file : la plus ancienne réservation en attente pour l'ouvrage. */
    Optional<Reservation> findFirstByOuvrageIdAndStatutOrderByDateReservationAscIdAsc(Long ouvrageId, StatutReservation statut);

    boolean existsByOuvrageIdAndAdherentIdAndStatutIn(Long ouvrageId, Long adherentId, Collection<StatutReservation> statuts);

    Optional<Reservation> findByExemplaireIdAndStatut(Long exemplaireId, StatutReservation statut);

    Page<Reservation> findByAdherentId(Long adherentId, Pageable pageable);
}
