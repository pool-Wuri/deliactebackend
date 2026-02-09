package com.deliacte.repository.stats;

import com.deliacte.entity.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganisationStatsRepository extends JpaRepository<Organisation, UUID> {

    // ===============================
    // SUPER ADMIN → toutes les organisations
    // ===============================
    @Query("SELECT COUNT(o) FROM Organisation o")
    long countAllOrganisations();

    // ==========================================
    // RESPONSABLE ORGANISATION → organisations où il est responsable
    // ==========================================
    @Query("""
            SELECT COUNT(DISTINCT o)
            FROM Organisation o
            JOIN o.users u
            WHERE u.id = :userId
            """)
    long countOrganisationsByResponsibleOrganisation(UUID userId);

    // ==========================================
    // RESPONSABLE PROCEDURE → organisations liées aux procédures qu’il administre
    // ==========================================
    @Query("""
            SELECT COUNT(DISTINCT o)
            FROM Organisation o
            JOIN o.procedures p
            JOIN p.users u
            WHERE u.id = :userId
            """)
    long countOrganisationsByResponsibleProcedures(UUID userId);
}
