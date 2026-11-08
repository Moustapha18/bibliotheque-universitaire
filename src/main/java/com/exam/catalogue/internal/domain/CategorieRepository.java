package com.exam.catalogue.internal.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByNomIgnoreCase(String nom);
}
