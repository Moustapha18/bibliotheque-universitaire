package com.exam.transactions.internal.service;

import com.exam.acteurs.api.AdherentApi;
import com.exam.acteurs.api.AdherentInfo;
import com.exam.catalogue.api.CatalogueApi;
import com.exam.shared.error.ConflictException;
import com.exam.shared.error.ResourceNotFoundException;
import com.exam.transactions.api.ReservationDisponible;
import com.exam.transactions.internal.domain.Reservation;
import com.exam.transactions.internal.domain.ReservationRepository;
import com.exam.transactions.internal.domain.StatutReservation;
import com.exam.transactions.internal.dto.ReservationRequest;
import com.exam.transactions.internal.dto.ReservationResponse;
import com.exam.transactions.internal.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static com.exam.transactions.internal.domain.StatutReservation.EN_ATTENTE;
import static com.exam.transactions.internal.domain.StatutReservation.PRETE;

/** Gère la file d'attente des réservations (premier arrivé, premier servi). */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private static final Set<StatutReservation> STATUTS_ACTIFS = EnumSet.of(EN_ATTENTE, PRETE);

    private final ReservationRepository repository;
    private final CatalogueApi catalogueApi;
    private final AdherentApi adherentApi;
    private final ApplicationEventPublisher eventPublisher;
    private final ReservationMapper mapper;
    private final Clock clock;

    public ReservationResponse reserver(ReservationRequest request) {
        AdherentInfo adherent = adherentApi.getAdherent(request.adherentId());
        if (!adherent.actif()) {
            throw new ConflictException("L'adhérent %d est désactivé".formatted(adherent.id()));
        }
        if (!catalogueApi.ouvrageExiste(request.ouvrageId())) {
            throw new ResourceNotFoundException("Ouvrage", request.ouvrageId());
        }
        if (catalogueApi.compterExemplairesDisponibles(request.ouvrageId()) > 0) {
            throw new ConflictException("Des exemplaires de l'ouvrage %d sont disponibles : empruntez-en un directement"
                    .formatted(request.ouvrageId()));
        }
        if (repository.existsByOuvrageIdAndAdherentIdAndStatutIn(request.ouvrageId(), adherent.id(), STATUTS_ACTIFS)) {
            throw new ConflictException("L'adhérent %d a déjà une réservation active sur l'ouvrage %d"
                    .formatted(adherent.id(), request.ouvrageId()));
        }
        Reservation reservation = Reservation.creer(request.ouvrageId(), adherent.id(), Instant.now(clock));
        return mapper.toResponse(repository.save(reservation));
    }

    /**
     * Au retour (ou à l'annulation d'une réservation prête) : l'exemplaire va au premier de la file ;
     * s'il n'y a personne, il redevient disponible.
     */
    public void redistribuerExemplaire(Long ouvrageId, Long exemplaireId) {
        Optional<Reservation> next = repository
                .findFirstByOuvrageIdAndStatutOrderByDateReservationAscIdAsc(ouvrageId, EN_ATTENTE);
        if (next.isEmpty()) {
            catalogueApi.marquerDisponible(exemplaireId);
            return;
        }
        Reservation reservation = next.get();
        reservation.marquerPrete(exemplaireId);
        catalogueApi.marquerReserve(exemplaireId);
        eventPublisher.publishEvent(new ReservationDisponible(
                reservation.getId(), reservation.getAdherentId(), ouvrageId, exemplaireId));
    }

    /** Un exemplaire RESERVE ne peut être emprunté que par le titulaire de la réservation prête. */
    public void verifierDetenteur(Long exemplaireId, Long adherentId) {
        boolean estDetenteur = repository.findByExemplaireIdAndStatut(exemplaireId, PRETE)
                .filter(reservation -> reservation.getAdherentId().equals(adherentId))
                .isPresent();
        if (!estDetenteur) {
            throw new ConflictException("L'exemplaire %d est réservé pour un autre adhérent".formatted(exemplaireId));
        }
    }

    public void honorerReservationPrete(Long exemplaireId, Long adherentId) {
        repository.findByExemplaireIdAndStatut(exemplaireId, PRETE)
                .filter(reservation -> reservation.getAdherentId().equals(adherentId))
                .ifPresent(Reservation::honorer);
    }

    public ReservationResponse annuler(Long id) {
        Reservation reservation = getOrThrow(id);
        boolean etaitPrete = reservation.estPrete();
        reservation.annuler();
        if (etaitPrete) {
            redistribuerExemplaire(reservation.getOuvrageId(), reservation.getExemplaireId());
        }
        return mapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> findAll(Long adherentId, Pageable pageable) {
        Page<Reservation> page = adherentId == null
                ? repository.findAll(pageable)
                : repository.findByAdherentId(adherentId, pageable);
        return page.map(mapper::toResponse);
    }

    private Reservation getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Réservation", id));
    }
}
