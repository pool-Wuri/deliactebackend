package com.deliacte.repository;

import com.deliacte.entity.Procedure;
import com.deliacte.enums.ProcedureStatus;
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
public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {

    Optional<Procedure> findByIdAndDeletedFalse(UUID id);

    Optional<Procedure> findByCodeAndDeletedFalse(String code);

    Boolean existsByCodeAndDeletedFalse(String code);

    Page<Procedure> findByDeletedFalse(Pageable pageable);

    Page<Procedure> findByStatusAndDeletedFalse(ProcedureStatus status, Pageable pageable);

    Page<Procedure> findByIsPublicTrueAndStatusAndDeletedFalse(ProcedureStatus status, Pageable pageable);

    Page<Procedure> findByOrganisationIdAndDeletedFalse(UUID organisationId, Pageable pageable);
    List<Procedure> findByOrganisationIdAndDeletedFalse(UUID organisationId);

    List<Procedure> findByOrganisationIdAndStatusAndDeletedFalse(UUID organisationId, ProcedureStatus status);

    @Query("SELECT p FROM Procedure p WHERE p.deleted = false AND p.isPublic = true AND p.status = 'ACTIVE'")
    Page<Procedure> findPublishedProcedures(Pageable pageable);

    @Query("SELECT p FROM Procedure p WHERE p.deleted = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Procedure> searchProcedures(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Procedure p WHERE p.deleted = false AND p.status = :status AND " +
           "p.allowedUserStatuses LIKE CONCAT('%', :userStatus, '%')")
    List<Procedure> findByAllowedUserStatusesContainingAndStatusAndDeletedFalse(
            @Param("userStatus") String userStatus, 
            @Param("status") ProcedureStatus status);

    @Query("SELECT p FROM Procedure p JOIN p.users u WHERE u.id = :userId AND p.deleted = false")
    Page<Procedure> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Procedure p WHERE p.deleted = false")
    long countActiveProcedures();

    @Query("SELECT COUNT(p) FROM Procedure p WHERE p.status = :status AND p.deleted = false")
    long countByStatus(@Param("status") ProcedureStatus status);

    @Query("SELECT COUNT(p) FROM Procedure p WHERE p.organisation.id = :organisationId AND p.deleted = false")
    long countByOrganisationId(@Param("organisationId") UUID organisationId);


    /**
     * Récupérer toutes les procédures accessibles par un utilisateur
     * (via ses organisations)
     */
    @Query("""
        SELECT DISTINCT p
        FROM Procedure p
        JOIN p.organisation o
        JOIN o.users u
        WHERE u.id = :userId
    """)
    List<Procedure> findByUserOrganisationId(UUID userId);

    /**
     * Variante avec email
     */
    @Query("""
        SELECT DISTINCT p
        FROM Procedure p
        JOIN p.organisation o
        JOIN o.users u
        WHERE u.email = :email
    """)
    List<Procedure> findByUserOrganisationEmail(String email);

    /**
     * Récupérer toutes les procédures accessibles par un utilisateur
     * (via ses organisations)
     */
    @Query("""
        SELECT DISTINCT p
        FROM Procedure p
        JOIN p.users u
        WHERE u.id = :userId
    """)
    List<Procedure> findByUserProcedureId(UUID userId);

    /**
     * Variante avec email
     */
    @Query("""
        SELECT DISTINCT p
        FROM Procedure p
        JOIN p.users u
        WHERE u.email = :email
    """)
    List<Procedure> findByUserProcedureEmail(String email);
    @Query("""
        SELECT DISTINCT p
        FROM User u
        JOIN u.operations o
        JOIN o.procedure p
        WHERE u.id = :userId
    """)
    List<Procedure> findProceduresByUserOperations(@Param("userId") UUID userId);

    @Query(
            value = """
        SELECT p
        FROM Procedure p
        JOIN p.operations o
        JOIN DossierOperation doo ON doo.operation = o
        JOIN doo.dossier d
        GROUP BY p
        ORDER BY COUNT(DISTINCT d.dossierNumber) DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT p.id)
        FROM Procedure p
        JOIN p.operations o
        JOIN DossierOperation doo ON doo.operation = o
        JOIN doo.dossier d
    """
    )
    Page<Procedure> findProceduresByDistinctDossierCountDesc(
            Pageable pageable
    );


    Optional<Procedure> findByCode(String s);
}
