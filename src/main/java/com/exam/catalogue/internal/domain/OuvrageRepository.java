package com.exam.catalogue.internal.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OuvrageRepository extends JpaRepository<Ouvrage, Long>, JpaSpecificationExecutor<Ouvrage> {

    Optional<Ouvrage> findByIsbn(String isbn);

    @Override
    @EntityGraph(attributePaths = "categorie")
    Page<Ouvrage> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "categorie")
    Page<Ouvrage> findAll(Specification<Ouvrage> spec, Pageable pageable);
}
