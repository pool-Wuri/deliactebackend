package com.deliacte.service;

import com.deliacte.dto.request.DossierChampValueRequest;
import com.deliacte.entity.DossierChampValue;
import com.deliacte.entity.DossierHistorique;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les valeurs des champs avec versioning.
 *
 * Ce service encapsule toute la logique de versioning des valeurs de champs :
 * - Quand une nouvelle valeur est soumise, l'ancienne est désactivée
 * - La nouvelle valeur devient active
 * - L'historique complet est conservé pour l'audit
 */
public interface DossierChampValueService {

    /**
     * Ajoute ou met à jour la valeur d'un champ avec versioning.
     *
     * Si une valeur active existe déjà pour ce champ, elle est désactivée.
     * La nouvelle valeur devient active avec le numéro de version suivant.
     *
     * @param request La requête contenant la nouvelle valeur du champ
     * @param dossierHistorique L'historique auquel cette valeur appartient
     * @return La nouvelle valeur créée
     */
    @Transactional
    DossierChampValue addOrUpdateChampValue(DossierChampValueRequest request, DossierHistorique dossierHistorique);

    /**
     * Récupère la valeur ACTIVE (courante) d'un champ pour un dossier donné.
     *
     * C'est la valeur qui est affichée dans le formulaire et utilisée dans le dossier.
     *
     * @param dossierId L'ID du dossier
     * @param champOperationId L'ID du champ
     * @return La valeur active du champ, ou null si aucune valeur n'existe
     */
    DossierChampValue getActiveValue(UUID dossierId, UUID champOperationId);

    /**
     * Récupère TOUTES les versions (actives et inactives) d'un champ pour un dossier.
     *
     * Utile pour afficher l'historique complet des modifications d'un champ.
     * Les versions sont triées de la plus récente à la plus ancienne.
     *
     * @param dossierId L'ID du dossier
     * @param champOperationId L'ID du champ
     * @return La liste de toutes les versions du champ
     */
    List<DossierChampValue> getAllVersions(UUID dossierId, UUID champOperationId);

    /**
     * Récupère toutes les valeurs ACTIVES d'un dossier.
     *
     * Cela permet d'afficher l'état courant du formulaire du dossier
     * avec les dernières valeurs soumises pour chaque champ.
     *
     * @param dossierId L'ID du dossier
     * @return La liste de toutes les valeurs actives du dossier
     */
    List<DossierChampValue> getAllActiveValuesByDossier(UUID dossierId);

    /**
     * Récupère toutes les valeurs soumises lors d'une action spécifique.
     *
     * Utile pour afficher les données exactes qui ont été soumises lors d'une étape donnée.
     *
     * @param dossierHistoriqueId L'ID de l'historique (action)
     * @return La liste des valeurs soumises lors de cette action
     */
    List<DossierChampValue> getValuesByHistorique(UUID dossierHistoriqueId);

    /**
     * Désactive toutes les valeurs actives d'un dossier.
     *
     * Utile lors d'un rejet ou d'une annulation pour marquer toutes les valeurs comme obsolètes.
     *
     * @param dossierId L'ID du dossier
     */
    @Transactional
    void deactivateAllValuesByDossier(UUID dossierId);

    /**
     * Récupère la valeur active d'un champ sous forme d'Optional.
     *
     * Variante de getActiveValue qui retourne un Optional pour une meilleure gestion des cas null.
     *
     * @param dossierId L'ID du dossier
     * @param champOperationId L'ID du champ
     * @return Un Optional contenant la valeur active, ou vide si aucune valeur n'existe
     */
    Optional<DossierChampValue> findActiveValue(UUID dossierId, UUID champOperationId);

    /**
     * Compte le nombre de versions d'un champ pour un dossier donné.
     *
     * @param dossierId L'ID du dossier
     * @param champOperationId L'ID du champ
     * @return Le nombre de versions
     */
    Integer countVersions(UUID dossierId, UUID champOperationId);
}
