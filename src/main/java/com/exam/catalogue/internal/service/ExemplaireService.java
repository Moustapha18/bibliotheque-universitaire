package com.exam.catalogue.internal.service;

import com.exam.catalogue.api.StatutExemplaire;
import com.exam.catalogue.internal.domain.Exemplaire;
import com.exam.catalogue.internal.domain.ExemplaireRepository;
import com.exam.catalogue.internal.domain.Ouvrage;
import com.exam.catalogue.internal.domain.OuvrageRepository;
import com.exam.catalogue.internal.dto.ExemplaireRequest;
import com.exam.catalogue.internal.dto.ExemplaireResponse;
import com.exam.catalogue.internal.mapper.ExemplaireMapper;
import com.exam.shared.error.ConflictException;
import com.exam.shared.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExemplaireService {

    private final ExemplaireRepository exemplaireRepository;
    private final OuvrageRepository ouvrageRepository;
    private final ExemplaireMapper mapper;

    public Page<ExemplaireResponse> findAll(Long ouvrageId, Pageable pageable) {
        Page<Exemplaire> page = ouvrageId == null
                ? exemplaireRepository.findAll(pageable)
                : exemplaireRepository.findByOuvrageId(ouvrageId, pageable);
        return page.map(mapper::toResponse);
    }

    public ExemplaireResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public ExemplaireResponse create(ExemplaireRequest request) {
        ensureBarcodeAvailable(request.codeBarre(), null);
        Exemplaire exemplaire = mapper.toEntity(request);
        exemplaire.setOuvrage(getOuvrageOrThrow(request.ouvrageId()));
        return mapper.toResponse(exemplaireRepository.save(exemplaire));
    }

    @Transactional
    public ExemplaireResponse update(Long id, ExemplaireRequest request) {
        Exemplaire exemplaire = getOrThrow(id);
        ensureBarcodeAvailable(request.codeBarre(), id);
        mapper.updateEntity(request, exemplaire);
        exemplaire.setOuvrage(getOuvrageOrThrow(request.ouvrageId()));
        return mapper.toResponse(exemplaire);
    }

    @Transactional
    public void delete(Long id) {
        Exemplaire exemplaire = getOrThrow(id);
        if (exemplaire.getStatut() != StatutExemplaire.DISPONIBLE) {
            throw new ConflictException("Impossible de supprimer l'exemplaire %d : il est %s"
                    .formatted(id, exemplaire.getStatut()));
        }
        exemplaireRepository.delete(exemplaire);
    }

    private Exemplaire getOrThrow(Long id) {
        return exemplaireRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exemplaire", id));
    }

    private Ouvrage getOuvrageOrThrow(Long ouvrageId) {
        return ouvrageRepository.findById(ouvrageId)
                .orElseThrow(() -> new ResourceNotFoundException("Ouvrage", ouvrageId));
    }

    private void ensureBarcodeAvailable(String codeBarre, Long currentId) {
        exemplaireRepository.findByCodeBarre(codeBarre)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Le code-barres %s est déjà utilisé".formatted(codeBarre));
                });
    }
}
