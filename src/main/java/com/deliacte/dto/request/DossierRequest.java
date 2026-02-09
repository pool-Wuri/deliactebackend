package com.deliacte.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierRequest {

    @NotNull(message = "L'ID de l'opération actuelle est obligatoire")
    private UUID currentOperationId;

    // Laisser null pour une nouvelle création, sinon le numéro du dossier existant
    private String numeroDossier;

    private String commentaire;

    @NotNull(message = "Le champ sendToNext est obligatoire")
    private Boolean sendToNext;

    // Utilisé uniquement si sendToNext = false (rejet vers une opération précédente)
    private List<UUID> previousOperationId;

    // Les valeurs des champs soumises
    private List<DossierChampValueRequest> champValues;
}
