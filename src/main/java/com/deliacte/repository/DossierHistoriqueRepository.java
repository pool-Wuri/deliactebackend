package com.deliacte.repository;

import com.deliacte.entity.DossierHistorique;
import com.deliacte.enums.DossierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DossierHistoriqueRepository extends JpaRepository<DossierHistorique, UUID> {

    List<DossierHistorique> findByDossierIdOrderByActionDateDesc(UUID dossierId);

    Page<DossierHistorique> findByDossierIdOrderByActionDateDesc(UUID dossierId, Pageable pageable);

    List<DossierHistorique> findByUserIdOrderByActionDateDesc(UUID userId);

    List<DossierHistorique> findByDossierIdAndStatusOrderByActionDateDesc(UUID dossierId, DossierStatus status);
}
