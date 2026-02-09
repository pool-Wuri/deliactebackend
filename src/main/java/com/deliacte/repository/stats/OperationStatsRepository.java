package com.deliacte.repository.stats;

import com.deliacte.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OperationStatsRepository extends JpaRepository<Operation, UUID> {

    // ===============================
    // SUPER ADMIN → toutes les opérations
    // ===============================
    @Query("SELECT COUNT(o) FROM Operation o")
    long countAllOperations();

    // ==========================================
    // RESPONSABLE ORGANISATION → opérations de ses organisations
    // ==========================================
    @Query("""
    SELECT COUNT(DISTINCT o)
    FROM Operation o
    JOIN o.procedure p
    JOIN p.organisation org
    JOIN org.users u
    WHERE u.id = :userId
    """)
    long countOperationsByOrganisationOfUser(UUID userId);

    // ==========================================
    // ADMIN PROCEDURE → opérations des procédures auxquelles il est affecté
    // ==========================================
    @Query("""
    SELECT COUNT(DISTINCT o)
    FROM Operation o
    JOIN o.procedure p
    JOIN p.users u
    WHERE u.id = :userId
    """)
    long countOperationsByProceduresOfUser(UUID userId);

    // ==========================================
    // AGENT / UTILISATEUR → opérations auxquelles il est directement lié
    // ==========================================
    @Query("""
    SELECT COUNT(DISTINCT o)
    FROM Operation o
    JOIN o.users u
    WHERE u.id = :userId
    """)
    long countOperationsAssignedToUser(UUID userId);
}
