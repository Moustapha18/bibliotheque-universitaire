package com.exam.catalogue.internal.domain;

import com.exam.catalogue.api.StatutExemplaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ExemplaireRepository extends JpaRepository<Exemplaire, Long> {

    Optional<Exemplaire> findByCodeBarre(String codeBarre);

    long countByOuvrageIdAndStatut(Long ouvrageId, StatutExemplaire statut);

    @EntityGraph(attributePaths = "ouvrage")
    Page<Exemplaire> findByOuvrageId(Long ouvrageId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "ouvrage")
    Page<Exemplaire> findAll(Pageable pageable);

    /**
     * Transition atomique de statut (UPDATE conditionnel) : protège contre deux emprunts simultanés
     * du même exemplaire sans verrou applicatif.
     *
     * @return nombre de lignes modifiées (0 si le statut courant n'est pas dans {@code sources})
     */
    @Modifying(flushAutomatically = true)
    @Query("update Exemplaire e set e.statut = :cible where e.id = :id and e.statut in :sources")
    int changerStatut(@Param("id") Long id,
                      @Param("cible") StatutExemplaire cible,
                      @Param("sources") Collection<StatutExemplaire> sources);
}
