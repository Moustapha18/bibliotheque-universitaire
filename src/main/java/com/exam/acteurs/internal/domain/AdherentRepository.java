package com.exam.acteurs.internal.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdherentRepository extends JpaRepository<Adherent, Long> {

    Optional<Adherent> findByEmailIgnoreCase(String email);
}
