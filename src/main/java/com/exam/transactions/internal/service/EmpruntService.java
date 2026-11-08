package com.exam.transactions.internal.service;

import com.exam.acteurs.api.AdherentApi;
import com.exam.acteurs.api.AdherentInfo;
import com.exam.catalogue.api.CatalogueApi;
import com.exam.catalogue.api.ExemplaireInfo;
import com.exam.shared.error.ConflictException;
import com.exam.shared.error.ResourceNotFoundException;
import com.exam.transactions.api.EmpruntEffectue;
import com.exam.transactions.api.RetourEffectue;
import com.exam.transactions.internal.domain.Emprunt;
import com.exam.transactions.internal.domain.EmpruntRepository;
import com.exam.transactions.internal.dto.EmpruntRequest;
import com.exam.transactions.internal.dto.EmpruntResponse;
import com.exam.transactions.internal.mapper.EmpruntMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final ReservationService reservationService;
    private final CatalogueApi catalogueApi;
    private final AdherentApi adherentApi;
    private final PenaltyCalculator penaltyCalculator;
    private final ApplicationEventPublisher eventPublisher;
    private final EmpruntMapper mapper;
    private final Clock clock;

    public EmpruntResponse emprunter(EmpruntRequest request) {
        AdherentInfo adherent = adherentApi.getAdherent(request.adherentId());
        ensureAdherentActive(adherent);
        ensureQuotaNotReached(adherent);
        ensureExemplaireBorrowable(catalogueApi.getExemplaire(request.exemplaireId()), adherent.id());

        catalogueApi.marquerEmprunte(request.exemplaireId());
        reservationService.honorerReservationPrete(request.exemplaireId(), adherent.id());

        LocalDate today = LocalDate.now(clock);
        Emprunt emprunt = empruntRepository.save(Emprunt.ouvrir(
                adherent.id(), request.exemplaireId(), today, penaltyCalculator.dueDate(today)));

        eventPublisher.publishEvent(new EmpruntEffectue(
                emprunt.getId(), emprunt.getAdherentId(), emprunt.getExemplaireId(), emprunt.getDateRetourPrevue()));
        return mapper.toResponse(emprunt);
    }

    public EmpruntResponse retourner(Long empruntId) {
        Emprunt emprunt = getOrThrow(empruntId);
        if (!emprunt.isEnCours()) {
            throw new ConflictException("L'emprunt %d a déjà été retourné".formatted(empruntId));
        }
        LocalDate today = LocalDate.now(clock);
        int lateDays = penaltyCalculator.lateDays(emprunt.getDateRetourPrevue(), today);
        BigDecimal penalty = penaltyCalculator.penalty(lateDays);
        emprunt.cloturer(today, lateDays, penalty);

        ExemplaireInfo exemplaire = catalogueApi.getExemplaire(emprunt.getExemplaireId());
        reservationService.redistribuerExemplaire(exemplaire.ouvrageId(), exemplaire.id());

        eventPublisher.publishEvent(new RetourEffectue(
                emprunt.getId(), emprunt.getAdherentId(), emprunt.getExemplaireId(), lateDays, penalty));
        return mapper.toResponse(emprunt);
    }

    @Transactional(readOnly = true)
    public EmpruntResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<EmpruntResponse> findAll(Long adherentId, Pageable pageable) {
        Page<Emprunt> page = adherentId == null
                ? empruntRepository.findAll(pageable)
                : empruntRepository.findByAdherentId(adherentId, pageable);
        return page.map(mapper::toResponse);
    }

    private void ensureAdherentActive(AdherentInfo adherent) {
        if (!adherent.actif()) {
            throw new ConflictException("L'adhérent %d est désactivé".formatted(adherent.id()));
        }
    }

    private void ensureQuotaNotReached(AdherentInfo adherent) {
        long enCours = empruntRepository.countByAdherentIdAndDateRetourEffectiveIsNull(adherent.id());
        if (enCours >= adherent.quotaMax()) {
            throw new ConflictException("Quota atteint : un %s ne peut avoir que %d emprunts simultanés"
                    .formatted(adherent.type(), adherent.quotaMax()));
        }
    }

    private void ensureExemplaireBorrowable(ExemplaireInfo exemplaire, Long adherentId) {
        switch (exemplaire.statut()) {
            case EMPRUNTE -> throw new ConflictException("L'exemplaire %d est déjà emprunté".formatted(exemplaire.id()));
            case RESERVE -> reservationService.verifierDetenteur(exemplaire.id(), adherentId);
            case DISPONIBLE -> { /* aucun contrôle supplémentaire */ }
        }
    }

    private Emprunt getOrThrow(Long id) {
        return empruntRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Emprunt", id));
    }
}
