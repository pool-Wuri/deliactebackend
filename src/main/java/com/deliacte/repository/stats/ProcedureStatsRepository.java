package com.deliacte.repository.stats;

import com.deliacte.entity.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProcedureStatsRepository extends JpaRepository<Procedure, UUID> {

    // ===============================
    // SUPER ADMIN → toutes les procédures
    // ===============================
    @Query("SELECT COUNT(p) FROM Procedure p")
    long countAllProcedures();

    // ==========================================
    // RESPONSABLE ORGANISATION → procédures de ses organisations
    // ==========================================
    @Query("""
    SELECT COUNT(DISTINCT p)
    FROM Procedure p
    JOIN p.organisation o
    JOIN o.users u
    WHERE u.id = :userId
""")
    long countProceduresByOrganisationOfUser(UUID userId);

    // ==========================================
    // ADMIN PROCEDURE → procédures auxquelles il est affecté
    // ==========================================
    @Query("""
    SELECT COUNT(DISTINCT p)
    FROM Procedure p
    JOIN p.users u
    WHERE u.id = :userId
""")
    long countProceduresAssignedToUser(UUID userId);


        @Query("""
    SELECT p.status, COUNT(p)
    FROM Procedure p
    GROUP BY p.status
    """)
        List<Object[]> countProceduresGroupedByStatus();



}
