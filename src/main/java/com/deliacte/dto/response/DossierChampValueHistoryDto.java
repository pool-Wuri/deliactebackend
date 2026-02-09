package com.deliacte.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierChampValueHistoryDto {

    // Identifiant de la valeur
    private UUID id;

    // Le champ auquel cette valeur appartient
    private UUID champOperationId;
    private String champLabel;

    // La valeur elle-même
    private String value;
    private String filePath;

    // Versioning
    private Integer version;
    private Boolean isActive;

    // Quand cette version a été créée
    private LocalDateTime createdAt;

    // Qui a soumis cette version
    private String submittedBy;

    // Commentaire de l'action
    private String actionComment;
}
