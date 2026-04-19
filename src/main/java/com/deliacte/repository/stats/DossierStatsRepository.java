package com.deliacte.repository.stats;

import com.deliacte.dto.*;
import com.deliacte.entity.Dossier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DossierStatsRepository extends CrudRepository<Dossier, UUID> {

    /**
     * Stats pour les dossiers des organisations de l'utilisateur
     */
    @Query("""
                SELECT
                    COUNT(DISTINCT d) AS totalDossiers,
                    SUM(CASE WHEN d.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS dossiersEnCours,
                    SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) AS dossiersTermines,
                    SUM(CASE WHEN d.status = 'REJECTED' THEN 1 ELSE 0 END) AS dossiersRejetes,
                    SUM(CASE WHEN EXISTS (
                        SELECT do FROM DossierOperation do
                        WHERE do.dossier = d AND do.status = 'PENDING'
                    ) THEN 1 ELSE 0 END) AS dossiersEnAttente
                FROM Dossier d
                JOIN d.procedure.organisation o
                JOIN o.users u
                WHERE u.id = :userId
            """)
    DossierStatsProjection countByUserOrganisations(@Param("userId") UUID userId);


    /**
     * Stats pour les dossiers des procédures de l'utilisateur
     */
    @Query("""
                SELECT
                    COUNT(DISTINCT d) AS totalDossiers,
                    SUM(CASE WHEN d.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS dossiersEnCours,
                    SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) AS dossiersTermines,
                    SUM(CASE WHEN d.status = 'REJECTED' THEN 1 ELSE 0 END) AS dossiersRejetes,
                    SUM(CASE WHEN EXISTS (
                        SELECT do FROM DossierOperation do
                        WHERE do.dossier = d AND do.status = 'PENDING'
                    ) THEN 1 ELSE 0 END) AS dossiersEnAttente
                FROM Dossier d
                JOIN d.procedure p
                JOIN p.users u
                WHERE u.id = :userId
            """)
    DossierStatsProjection countByUserProcedures(@Param("userId") UUID userId);


    /**
     * Stats pour les dossiers des opérations de l'utilisateur
     */
    @Query("""
                SELECT
                    COUNT(DISTINCT d) AS totalDossiers,
                    SUM(CASE WHEN d.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS dossiersEnCours,
                    SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) AS dossiersTermines,
                    SUM(CASE WHEN d.status = 'REJECTED' THEN 1 ELSE 0 END) AS dossiersRejetes,
                    SUM(CASE WHEN EXISTS (
                        SELECT do FROM DossierOperation do
                        WHERE do.dossier = d AND do.status = 'PENDING'
                    ) THEN 1 ELSE 0 END) AS dossiersEnAttente
                FROM Dossier d
                JOIN d.procedure.operations op
                JOIN op.users u
                WHERE u.id = :userId
            """)
    DossierStatsProjection countByUserOperations(@Param("userId") UUID userId);


    /**
     * Stats globales pour un super-admin (tout le monde)
     */
    @Query("""
                SELECT
                    COUNT(DISTINCT d) AS totalDossiers,
                    SUM(CASE WHEN d.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS dossiersEnCours,
                    SUM(CASE WHEN d.status = 'COMPLETED' THEN 1 ELSE 0 END) AS dossiersTermines,
                    SUM(CASE WHEN d.status = 'REJECTED' THEN 1 ELSE 0 END) AS dossiersRejetes,
                    SUM(CASE WHEN EXISTS (
                        SELECT do FROM DossierOperation do
                        WHERE do.dossier = d AND do.status = 'PENDING'
                    ) THEN 1 ELSE 0 END) AS dossiersEnAttente
                FROM Dossier d
            """)
    DossierStatsProjection countAllDossiers();

    // =============================== // Comptage par statut // ===============================
    @Query(" SELECT d.status, COUNT(d) FROM Dossier d GROUP BY d.status ")
    List<Object[]> countDossiersGroupedByStatus();

    // =============================== // Comptage par mois de soumission // ===============================
    @Query(" SELECT FUNCTION('MONTH', d.submittedAt), COUNT(d) FROM Dossier d WHERE d.submittedAt IS NOT NULL GROUP BY FUNCTION('MONTH', d.submittedAt) ORDER BY FUNCTION('MONTH', d.submittedAt) ")
    List<Object[]> countDossiersGroupedByMonth();


    // stats  de histogramme

    @Query("SELECT COUNT(d) FROM Dossier d")
    Long countTotalDossiers();


    @Query("""
            SELECT YEAR(d.createdAt) as year,
                   MONTH(d.createdAt) as month,
                   COUNT(DISTINCT d.dossierNumber) as totalCreated
            FROM Dossier d
            GROUP BY YEAR(d.createdAt), MONTH(d.createdAt)
            ORDER BY YEAR(d.createdAt), MONTH(d.createdAt)
            """)
    List<MonthlyCreatedProjection> monthlyCreated();

    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
                   COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            WHERE o.status = 'COMPLETED'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlyCompleted();


    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
               COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            WHERE o.status = 'PENDING'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlyPending();

    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
               COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            WHERE o.status = 'REJECTED'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlyRejected();

    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
               COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            WHERE o.status = 'SKIPPED'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlySkipped();


    @Query("""
            SELECT p.name as procedureName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            GROUP BY p.name
            """)
    List<TypeStatsProjection> countByProcedure();


    @Query("""
            SELECT o.name as organisationName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.organisation o
            GROUP BY o.name
            """)
    List<OrganisationStatsProjection> countByOrganisation();


    @Query("""
            SELECT YEAR(d.createdAt) as year,
                   MONTH(d.createdAt) as month,
                   COUNT(DISTINCT d.dossierNumber) as totalCreated
            FROM Dossier d
            JOIN d.procedure.organisation o
            JOIN o.users u
            WHERE u.id = :userId
            GROUP BY YEAR(d.createdAt), MONTH(d.createdAt)
            ORDER BY YEAR(d.createdAt), MONTH(d.createdAt)
            """)
    List<MonthlyCreatedProjection> monthlyCreatedByUserOrganisations(@Param("userId") UUID userId);

    @Query("""
            SELECT YEAR(d.createdAt) as year,
                   MONTH(d.createdAt) as month,
                   COUNT(DISTINCT d.dossierNumber) as totalCreated
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.users u
            WHERE u.id = :userId
            GROUP BY YEAR(d.createdAt), MONTH(d.createdAt)
            ORDER BY YEAR(d.createdAt), MONTH(d.createdAt)
            """)
    List<MonthlyCreatedProjection> monthlyCreatedByUserProcedures(@Param("userId") UUID userId);

    @Query("""
            SELECT YEAR(d.createdAt) as year,
                   MONTH(d.createdAt) as month,
                   COUNT(DISTINCT d.dossierNumber) as totalCreated
            FROM Dossier d
            JOIN d.procedure.operations op
            JOIN op.users u
            WHERE u.id = :userId
            GROUP BY YEAR(d.createdAt), MONTH(d.createdAt)
            ORDER BY YEAR(d.createdAt), MONTH(d.createdAt)
            """)
    List<MonthlyCreatedProjection> monthlyCreatedByUserOperations(@Param("userId") UUID userId);

    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
                   COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            JOIN o.dossier d
            JOIN d.procedure.organisation org
            JOIN org.users u
            WHERE u.id = :userId AND o.status = 'PENDING'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlyPendingByUserOrganisations(@Param("userId") UUID userId);

    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
                   COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            JOIN o.dossier d
            JOIN d.procedure p
            JOIN p.users u
            WHERE u.id = :userId AND o.status = 'REJECTED'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlyRejectedByUserProcedures(@Param("userId") UUID userId);

    @Query("""
            SELECT YEAR(o.completedAt) as year,
                   MONTH(o.completedAt) as month,
                   COUNT(DISTINCT o.dossier.dossierNumber) as total
            FROM DossierOperation o
            JOIN o.dossier d
            JOIN d.procedure.operations op
            JOIN op.users u
            WHERE u.id = :userId AND o.status = 'SKIPPED'
            GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
            ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
            """)
    List<MonthlyStatusProjection> monthlySkippedByUserOperations(@Param("userId") UUID userId);


    @Query("""
            SELECT p.name as procedureName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.organisation o
            JOIN o.users u
            WHERE u.id = :userId
            GROUP BY p.name
            """)
    List<TypeStatsProjection> countByProcedureUserOrganisations(@Param("userId") UUID userId);

    @Query("""
            SELECT p.name as procedureName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.users u
            WHERE u.id = :userId
            GROUP BY p.name
            """)
    List<TypeStatsProjection> countByProcedureUserProcedures(@Param("userId") UUID userId);

    @Query("""
            SELECT p.name as procedureName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.operations op
            JOIN op.users u
            WHERE u.id = :userId
            GROUP BY p.name
            """)
    List<TypeStatsProjection> countByProcedureUserOperations(@Param("userId") UUID userId);

    @Query("""
            SELECT o.name as organisationName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.organisation o
            JOIN o.users u
            WHERE u.id = :userId
            GROUP BY o.name
            """)
    List<OrganisationStatsProjection> countByOrganisationUserOrganisations(@Param("userId") UUID userId);

    @Query("""
            SELECT o.name as organisationName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.users u
            JOIN p.organisation o
            WHERE u.id = :userId
            GROUP BY o.name
            """)
    List<OrganisationStatsProjection> countByOrganisationUserProcedures(@Param("userId") UUID userId);

    @Query("""
            SELECT o.name as organisationName,
                   COUNT(DISTINCT d.dossierNumber) as total
            FROM Dossier d
            JOIN d.procedure p
            JOIN p.operations op
            JOIN op.users u
            JOIN p.organisation o
            WHERE u.id = :userId
            GROUP BY o.name
            """)
    List<OrganisationStatsProjection> countByOrganisationUserOperations(@Param("userId") UUID userId);

    @Query("""
    SELECT YEAR(o.completedAt) as year,
           MONTH(o.completedAt) as month,
           COUNT(DISTINCT o.dossier.dossierNumber) as total
    FROM DossierOperation o
    JOIN o.dossier d
    JOIN d.procedure p
    JOIN p.users u
    WHERE u.id = :userId AND o.status = 'COMPLETED'
    GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
    ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
    """)
    List<MonthlyStatusProjection> monthlyCompletedByUserProcedures(@Param("userId") UUID userId);


    @Query("""
    SELECT YEAR(o.completedAt) as year,
           MONTH(o.completedAt) as month,
           COUNT(DISTINCT o.dossier.dossierNumber) as total
    FROM DossierOperation o
    JOIN o.dossier d
    JOIN d.procedure p
    JOIN p.users u
    WHERE u.id = :userId AND o.status = 'PENDING'
    GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
    ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
    """)
    List<MonthlyStatusProjection> monthlyPendingByUserProcedures(@Param("userId") UUID userId);


    @Query("""
    SELECT YEAR(o.completedAt) as year,
           MONTH(o.completedAt) as month,
           COUNT(DISTINCT o.dossier.dossierNumber) as total
    FROM DossierOperation o
    JOIN o.dossier d
    JOIN d.procedure p
    JOIN p.users u
    WHERE u.id = :userId AND o.status = 'SKIPPED'
    GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
    ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
    """)
    List<MonthlyStatusProjection> monthlySkippedByUserProcedures(@Param("userId") UUID userId);

    @Query("""
    SELECT YEAR(o.completedAt) as year,
           MONTH(o.completedAt) as month,
           COUNT(DISTINCT o.dossier.dossierNumber) as total
    FROM DossierOperation o
    JOIN o.dossier d
    JOIN d.procedure p
    JOIN p.organisation org
    JOIN org.users u
    WHERE u.id = :userId AND o.status = 'COMPLETED'
    GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
    ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
    """)
    List<MonthlyStatusProjection> monthlyCompletedByUserOrganisations(@Param("userId") UUID userId);


    @Query("""
    SELECT YEAR(o.completedAt) as year,
           MONTH(o.completedAt) as month,
           COUNT(DISTINCT o.dossier.dossierNumber) as total
    FROM DossierOperation o
    JOIN o.dossier d
    JOIN d.procedure p
    JOIN p.organisation org
    JOIN org.users u
    WHERE u.id = :userId AND o.status = 'REJECTED'
    GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
    ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
    """)
    List<MonthlyStatusProjection> monthlyRejectedByUserOrganisations(@Param("userId") UUID userId);


    @Query("""
    SELECT YEAR(o.completedAt) as year,
           MONTH(o.completedAt) as month,
           COUNT(DISTINCT o.dossier.dossierNumber) as total
    FROM DossierOperation o
    JOIN o.dossier d
    JOIN d.procedure p
    JOIN p.organisation org
    JOIN org.users u
    WHERE u.id = :userId AND o.status = 'SKIPPED'
    GROUP BY YEAR(o.completedAt), MONTH(o.completedAt)
    ORDER BY YEAR(o.completedAt), MONTH(o.completedAt)
    """)
    List<MonthlyStatusProjection> monthlySkippedByUserOrganisations(@Param("userId") UUID userId);



}
