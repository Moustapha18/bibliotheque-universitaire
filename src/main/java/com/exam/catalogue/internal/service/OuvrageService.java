package com.exam.catalogue.internal.service;

import com.exam.catalogue.internal.domain.Categorie;
import com.exam.catalogue.internal.domain.CategorieRepository;
import com.exam.catalogue.internal.domain.Ouvrage;
import com.exam.catalogue.internal.domain.OuvrageRepository;
import com.exam.catalogue.internal.domain.OuvrageSearchCritere;
import com.exam.catalogue.internal.domain.OuvrageSpecifications;
import com.exam.catalogue.internal.dto.OuvrageRequest;
import com.exam.catalogue.internal.dto.OuvrageResponse;
import com.exam.catalogue.internal.mapper.OuvrageMapper;
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
public class OuvrageService {

    private final OuvrageRepository ouvrageRepository;
    private final CategorieRepository categorieRepository;
    private final OuvrageMapper mapper;

    public Page<OuvrageResponse> findAll(Pageable pageable) {
        return ouvrageRepository.findAll(pageable).map(mapper::toResponse);
    }

    public Page<OuvrageResponse> search(OuvrageSearchCritere criteria, Pageable pageable) {
        return ouvrageRepository.findAll(OuvrageSpecifications.from(criteria), pageable).map(mapper::toResponse);
    }

    public OuvrageResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public OuvrageResponse create(OuvrageRequest request) {
        ensureIsbnAvailable(request.isbn(), null);
        Ouvrage ouvrage = mapper.toEntity(request);
        ouvrage.setCategorie(getCategorieOrThrow(request.categorieId()));
        return mapper.toResponse(ouvrageRepository.save(ouvrage));
    }

    @Transactional
    public OuvrageResponse update(Long id, OuvrageRequest request) {
        Ouvrage ouvrage = getOrThrow(id);
        ensureIsbnAvailable(request.isbn(), id);
        mapper.updateEntity(request, ouvrage);
        ouvrage.setCategorie(getCategorieOrThrow(request.categorieId()));
        return mapper.toResponse(ouvrage);
    }

    @Transactional
    public void delete(Long id) {
        ouvrageRepository.delete(getOrThrow(id));
        ouvrageRepository.flush();
    }

    private Ouvrage getOrThrow(Long id) {
        return ouvrageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ouvrage", id));
    }

    private Categorie getCategorieOrThrow(Long categorieId) {
        return categorieRepository.findById(categorieId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", categorieId));
    }

    private void ensureIsbnAvailable(String isbn, Long currentId) {
        ouvrageRepository.findByIsbn(isbn)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Un ouvrage avec l'ISBN %s existe déjà".formatted(isbn));
                });
    }
}
