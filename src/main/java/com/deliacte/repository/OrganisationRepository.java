package com.deliacte.repository;

import com.deliacte.entity.Organisation;
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
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findByIdAndDeletedFalse(UUID id);

    Optional<Organisation> findByCodeAndDeletedFalse(String code);

    Page<Organisation> findByDeletedFalse(Pageable pageable);

    Page<Organisation> findByIsActiveAndDeletedFalse(Boolean isActive, Pageable pageable);

    List<Organisation> findByParentIdAndDeletedFalse(UUID parentId);

    List<Organisation> findByParentIsNullAndDeletedFalse();

    @Query("SELECT o FROM Organisation o WHERE o.deleted = false AND " +
           "(LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Organisation> searchOrganisations(@Param("search") String search, Pageable pageable);

    Boolean existsByCodeAndDeletedFalse(String code);

    @Query("SELECT COUNT(o) FROM Organisation o WHERE o.deleted = false")
    long countActiveOrganisations();

    List<Organisation> findAllByDeletedFalse();

    @Query("""
    SELECT DISTINCT o
    FROM Organisation o
    JOIN FETCH o.procedures p
    JOIN o.users u
    WHERE u.id = :userId
""")
    List<Organisation> findOrganisationsWithProceduresByUserId(UUID userId);

    /**
     * Organisations + procédures par email utilisateur
     */
    @Query("""
        SELECT DISTINCT o
        FROM Organisation o
        JOIN FETCH o.procedures p
        JOIN o.users u
        WHERE u.email = :email
    """)
    List<Organisation> findOrganisationsWithProceduresByUserEmail(String email);


}
