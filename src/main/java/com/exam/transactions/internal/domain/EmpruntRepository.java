package com.exam.transactions.internal.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    long countByAdherentIdAndDateRetourEffectiveIsNull(Long adherentId);

    Page<Emprunt> findByAdherentId(Long adherentId, Pageable pageable);
}
