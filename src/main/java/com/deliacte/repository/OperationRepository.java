package com.deliacte.repository;

import com.deliacte.entity.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    Optional<Operation> findByIdAndDeletedFalse(UUID id);

    Page<Operation> findAllByDeletedFalse(Pageable pageable);

    List<Operation> findByProcedureIdAndDeletedFalseOrderByOrderIndexAsc(UUID procedureId);
    
    List<Operation> findByProcedureIdAndIsActiveAndDeletedFalseOrderByOrderIndexAsc(UUID procedureId, Boolean isActive);

    Page<Operation> findByProcedureIdAndDeletedFalse(UUID procedureId, Pageable pageable);

    Optional<Operation> findFirstByProcedureIdAndDeletedFalseOrderByOrderIndexAsc(UUID procedureId);

    @Query("SELECT o FROM Operation o JOIN o.users u WHERE u.id = :userId AND o.deleted = false")
    List<Operation> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT o FROM Operation o WHERE o.typeOperation.id = :typeOperationId AND o.deleted = false")
    List<Operation> findByTypeOperationId(@Param("typeOperationId") UUID typeOperationId);

    @Query("SELECT MAX(o.orderIndex) FROM Operation o WHERE o.procedure.id = :procedureId AND o.deleted = false")
    Integer findMaxOrderIndexByProcedureId(@Param("procedureId") UUID procedureId);

    @Query("SELECT COUNT(o) FROM Operation o WHERE o.procedure.id = :procedureId AND o.deleted = false")
    long countByProcedureIdAndDeletedFalse(@Param("procedureId") UUID procedureId);
    
    long countByDeletedFalse();
    
    Boolean existsByNameAndProcedureIdAndDeletedFalse(String name, UUID procedureId);
    @Query("""
    SELECT DISTINCT o
    FROM User u
    JOIN u.organisations org
    JOIN org.procedures p
    JOIN p.operations o
    WHERE u.id = :userId
""")
    List<Operation> findOperationsFromUserOrganisations(UUID userId);

    @Query("""
    SELECT DISTINCT o
    FROM User u
    JOIN u.procedures p
    JOIN p.operations o
    WHERE u.id = :userId
""")
    List<Operation> findOperationsFromUserProcedures(UUID userId);

    @Query("""
    SELECT DISTINCT o
    FROM Operation o
    JOIN o.users u
    WHERE u.id = :userId  
""")
    List<Operation> findOperationsFromUserAgent(UUID userId);


    @Query("""
    SELECT DISTINCT o
    FROM Procedure p
    JOIN p.operations o
    WHERE p.id = :procedureId
""")
    List<Operation> findOperationsByProcedureId(UUID procedureId);

    @Query("""
    SELECT DISTINCT o
    FROM Organisation org
    JOIN org.procedures p
    JOIN p.operations o
    WHERE org.id = :organisationId
""")
    List<Operation> findOperationsByOrganisationId(UUID organisationId);



    /**
     * Opérations SUIVANTES d'une opération
     */
    @Query("""
        SELECT o2
        FROM Operation o
        JOIN o.nextOperations o2
        WHERE o.id = :operationId
    """)
    List<Operation> findNextOperations(UUID operationId);

    /**
     * Opérations PRÉCÉDENTES d'une opération
     */
    @Query("""
        SELECT o2
        FROM Operation o
        JOIN o.previousOperations o2
        WHERE o.id = :operationId
    """)
    List<Operation> findPreviousOperations(UUID operationId);

    /**
     * Qui a cette opération comme suivante
     */
    @Query("""
    SELECT o
    FROM Operation o
    JOIN o.nextOperations n
    WHERE n.id = :operationId
""")
    List<Operation> findOperationsWhereIAmNext(UUID operationId);

    /**
     * Qui a cette opération comme précédente
     */
    @Query("""
    SELECT o
    FROM Operation o
    JOIN o.previousOperations p
    WHERE p.id = :operationId
""")
    List<Operation> findOperationsWhereIAmPrevious(UUID operationId);


    // Récupère la première opération d'une procédure (sans opération précédente)
    Optional<Operation> findFirstByProcedureIdAndPreviousOperationsIsEmpty(UUID procedureId);}
