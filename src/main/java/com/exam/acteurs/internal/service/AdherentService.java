package com.exam.acteurs.internal.service;

import com.exam.acteurs.internal.domain.Adherent;
import com.exam.acteurs.internal.domain.AdherentRepository;
import com.exam.acteurs.internal.dto.AdherentRequest;
import com.exam.acteurs.internal.dto.AdherentResponse;
import com.exam.acteurs.internal.mapper.AdherentMapper;
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
public class AdherentService {

    private final AdherentRepository repository;
    private final AdherentMapper mapper;

    public Page<AdherentResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public AdherentResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public AdherentResponse create(AdherentRequest request) {
        ensureEmailAvailable(request.email(), null);
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public AdherentResponse update(Long id, AdherentRequest request) {
        Adherent adherent = getOrThrow(id);
        ensureEmailAvailable(request.email(), id);
        mapper.updateEntity(request, adherent);
        return mapper.toResponse(adherent);
    }

    /** Suppression logique : l'historique des emprunts de l'adhérent reste cohérent. */
    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).setActif(false);
    }

    Adherent getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Adhérent", id));
    }

    private void ensureEmailAvailable(String email, Long currentId) {
        repository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("L'e-mail %s est déjà utilisé".formatted(email));
                });
    }
}
