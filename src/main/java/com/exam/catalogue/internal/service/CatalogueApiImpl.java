package com.exam.catalogue.internal.service;

import com.exam.catalogue.api.CatalogueApi;
import com.exam.catalogue.api.ExemplaireInfo;
import com.exam.catalogue.api.StatutExemplaire;
import com.exam.catalogue.internal.domain.ExemplaireRepository;
import com.exam.catalogue.internal.domain.OuvrageRepository;
import com.exam.shared.error.ConflictException;
import com.exam.shared.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.exam.catalogue.api.StatutExemplaire.DISPONIBLE;
import static com.exam.catalogue.api.StatutExemplaire.EMPRUNTE;
import static com.exam.catalogue.api.StatutExemplaire.RESERVE;

/** Implémentation de l'API publique du module (visible uniquement dans le package internal). */
@Service
@RequiredArgsConstructor
@Transactional
public class CatalogueApiImpl implements CatalogueApi {

    private final ExemplaireRepository exemplaireRepository;
    private final OuvrageRepository ouvrageRepository;

    @Override
    @Transactional(readOnly = true)
    public ExemplaireInfo getExemplaire(Long exemplaireId) {
        return exemplaireRepository.findById(exemplaireId)
                .map(e -> new ExemplaireInfo(e.getId(), e.getOuvrage().getId(), e.getStatut()))
                .orElseThrow(() -> new ResourceNotFoundException("Exemplaire", exemplaireId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ouvrageExiste(Long ouvrageId) {
        return ouvrageRepository.existsById(ouvrageId);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterExemplairesDisponibles(Long ouvrageId) {
        return exemplaireRepository.countByOuvrageIdAndStatut(ouvrageId, DISPONIBLE);
    }

    @Override
    public void marquerEmprunte(Long exemplaireId) {
        changerStatut(exemplaireId, EMPRUNTE, Set.of(DISPONIBLE, RESERVE));
    }

    @Override
    public void marquerReserve(Long exemplaireId) {
        changerStatut(exemplaireId, RESERVE, Set.of(EMPRUNTE, DISPONIBLE));
    }

    @Override
    public void marquerDisponible(Long exemplaireId) {
        changerStatut(exemplaireId, DISPONIBLE, Set.of(EMPRUNTE, RESERVE));
    }

    private void changerStatut(Long exemplaireId, StatutExemplaire cible, Set<StatutExemplaire> sources) {
        int updated = exemplaireRepository.changerStatut(exemplaireId, cible, sources);
        if (updated == 0) {
            if (!exemplaireRepository.existsById(exemplaireId)) {
                throw new ResourceNotFoundException("Exemplaire", exemplaireId);
            }
            throw new ConflictException("L'exemplaire %d ne peut pas passer à l'état %s".formatted(exemplaireId, cible));
        }
    }
}
