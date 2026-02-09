package com.deliacte.repository;

import com.deliacte.entity.DossierChampValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierChampValueRepository extends JpaRepository<DossierChampValue, UUID> {

    /**
     * Récupère la valeur ACTIVE (courante) d'un champ pour un dossier donné.
     * C'est la requête la plus importante : elle retourne la dernière version active du champ.
     */
    @Query("SELECT dcv FROM DossierChampValue dcv " +
            "WHERE dcv.dossierHistorique.dossier.id = :dossierId " +
            "AND dcv.champOperation.id = :champOperationId " +
            "AND dcv.isActive = true")
    Optional<DossierChampValue> findActiveValueByDossierAndChamp(
            @Param("dossierId") UUID dossierId,
            @Param("champOperationId") UUID champOperationId
    );

    /**
     * Récupère TOUTES les versions (actives et inactives) d'un champ pour un dossier.
     * Utile pour afficher l'historique des modifications d'un champ.
     */
    @Query("SELECT dcv FROM DossierChampValue dcv " +
            "WHERE dcv.dossierHistorique.dossier.id = :dossierId " +
            "AND dcv.champOperation.id = :champOperationId " +
            "ORDER BY dcv.version DESC")
    List<DossierChampValue> findAllVersionsByDossierAndChamp(
            @Param("dossierId") UUID dossierId,
            @Param("champOperationId") UUID champOperationId
    );

    /**
     * Récupère toutes les valeurs ACTIVES d'un dossier (pour afficher l'état courant du formulaire).
     */
    @Query("SELECT dcv FROM DossierChampValue dcv " +
            "WHERE dcv.dossierHistorique.dossier.id = :dossierId " +
            "AND dcv.isActive = true")
    List<DossierChampValue> findAllActiveValuesByDossier(
            @Param("dossierId") UUID dossierId
    );

    /**
     * Récupère toutes les valeurs soumises lors d'une action spécifique (DossierHistorique).
     */
    List<DossierChampValue> findByDossierHistoriqueId(UUID dossierHistoriqueId);
}
