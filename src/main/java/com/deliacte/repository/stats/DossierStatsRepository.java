package com.deliacte.repository.stats;

import com.deliacte.dto.DossierStatsProjection;
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


}
