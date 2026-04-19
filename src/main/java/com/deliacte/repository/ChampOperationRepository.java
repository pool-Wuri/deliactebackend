package com.deliacte.repository;

import com.deliacte.entity.ChampOperation;
import com.deliacte.entity.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChampOperationRepository extends JpaRepository<ChampOperation, UUID> {

    Optional<ChampOperation> findByIdAndDeletedFalse(UUID id);

    Page<ChampOperation> findAllByDeletedFalse(Pageable pageable);

    List<ChampOperation> findByOperationIdAndDeletedFalseOrderByOrderIndexAsc(UUID operationId);

    List<ChampOperation> findByEntityObjectIdAndDeletedFalseOrderByOrderIndexAsc(UUID entityObjectId);


    List<ChampOperation> findByOperationIdAndActiveAndDeletedFalseOrderByOrderIndexAsc(UUID operationId, Boolean active);


    List<ChampOperation> findByEntityObjectIdAndDeletedFalse(UUID entityObjectId);

    Boolean existsByCodeAndOperationIdAndDeletedFalse(String code, UUID operationId);

    @Query("SELECT MAX(c.orderIndex) FROM ChampOperation c WHERE c.operation.id = :operationId AND c.deleted = false")
    Integer findMaxOrderIndexByOperationId(@Param("operationId") UUID operationId);

    long countByOperationIdAndDeletedFalse(UUID operationId);

    @Modifying
    @Query("""
                UPDATE OptionChampOperation o
                SET o.isDefault = false
                WHERE o.champOperation.id = :champOperationId
            """)
    void unsetDefaultByChampOperation(@Param("champOperationId") UUID champOperationId);

    List<ChampOperation> findByEntityObjectId(UUID entityObjectId);

    @Query("""
            SELECT co
            FROM ChampOperation co
            JOIN FETCH co.entityObject eo
            LEFT JOIN FETCH co.options
            WHERE co.operation.id = :operationId
            AND co.entityObject IS NOT NULL
            AND co.active = true
            ORDER BY eo.id, co.orderIndex
            """)
    List<ChampOperation> findByOperationGroupedByEntity(@Param("operationId") UUID operationId);


    @Query("""
SELECT co
FROM ChampOperation co
LEFT JOIN FETCH co.options
WHERE co.operation.id = :operationId
AND co.entityObject IS NULL
AND co.active = true
ORDER BY co.order
""")
    List<ChampOperation> findGlobalByOperation(@Param("operationId") UUID operationId);

    /**
     * Récupère les champs liés à des entités pour une procédure donnée
     * et les groupe ensuite par EntityObject côté service.
     */
    @Query("SELECT c FROM ChampOperation c " +
            "WHERE c.operation.procedure.id = :procedureId " +
            "AND c.entityObject IS NOT NULL " +
            "AND c.deleted = false " +
            "ORDER BY c.orderIndex ASC")
    List<ChampOperation> findByProcedureGroupedByEntity(@Param("procedureId") UUID procedureId);

    /**
     * Récupère les champs globaux (sans entité) pour une procédure donnée
     */
    @Query("SELECT c FROM ChampOperation c " +
            "WHERE c.operation.procedure.id = :procedureId " +
            "AND c.entityObject IS NULL " +
            "AND c.deleted = false " +
            "ORDER BY c.orderIndex ASC")
    List<ChampOperation> findGlobalByProcedure(@Param("procedureId") UUID procedureId);


    Optional<ChampOperation> findByLabelAndOperation(String nomComplet, Operation op1);
}
