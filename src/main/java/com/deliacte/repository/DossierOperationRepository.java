package com.deliacte.repository;

import com.deliacte.entity.DossierOperation;
import com.deliacte.enums.DossierOperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierOperationRepository extends JpaRepository<DossierOperation, UUID> {

    @Query("SELECT do FROM DossierOperation do JOIN FETCH do.dossier d WHERE do.operation.id = :operationId AND do.status = :status")
    List<DossierOperation> findByOperationIdAndStatus(
            @Param("operationId") UUID operationId,
            @Param("status") DossierOperationStatus status
    );


    @Query("SELECT do FROM DossierOperation do JOIN FETCH do.dossier d WHERE do.operation.id = :operationId ")
    List<DossierOperation> findByOperationId(
            @Param("operationId") UUID operationId
    );



    Optional<DossierOperation> findByDossierIdAndOperationIdAndStatus(
            UUID dossierId,
            UUID operationId,
            DossierOperationStatus status
    );

    List<DossierOperation> findByDossierIdOrderByReceivedAtAsc(UUID dossierId);

    Optional<DossierOperation> findFirstByDossierIdAndStatusOrderByReceivedAtDesc(
            UUID dossierId,
            DossierOperationStatus status
    );


}
