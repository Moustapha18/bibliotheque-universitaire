package com.exam.catalogue.internal.service;

import com.exam.catalogue.internal.domain.Categorie;
import com.exam.catalogue.internal.domain.CategorieRepository;
import com.exam.catalogue.internal.dto.CategorieRequest;
import com.exam.catalogue.internal.dto.CategorieResponse;
import com.exam.catalogue.internal.mapper.CategorieMapper;
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
public class CategorieService {

    private final CategorieRepository repository;
    private final CategorieMapper mapper;

    public Page<CategorieResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public CategorieResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public CategorieResponse create(CategorieRequest request) {
        ensureNameAvailable(request.nom(), null);
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public CategorieResponse update(Long id, CategorieRequest request) {
        Categorie categorie = getOrThrow(id);
        ensureNameAvailable(request.nom(), id);
        mapper.updateEntity(request, categorie);
        return mapper.toResponse(categorie);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush();
    }

    private Categorie getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Catégorie", id));
    }

    private void ensureNameAvailable(String nom, Long currentId) {
        repository.findByNomIgnoreCase(nom)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Une catégorie nommée '%s' existe déjà".formatted(nom));
                });
    }
}
