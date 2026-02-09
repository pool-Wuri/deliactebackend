package com.deliacte.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureRequest {

    @NotBlank(message = "Le nom de la procédure est obligatoire")
    @Size(min = 2, max = 255, message = "Le nom doit contenir entre 2 et 255 caractères")
    private String name;

    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    private String code;

    @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
    private String description;

    @Size(max = 2000, message = "Les prérequis ne doivent pas dépasser 2000 caractères")
    private String requirements;

    private String processingTime;

    private BigDecimal fee;

    private Boolean hasPayment;

    private Boolean isPublic;

    // Statuts d'utilisateurs autorisés (séparés par des virgules)
    // Ex: "CITOYEN,RESIDENT,REFUGIE"
    private String allowedUserStatuses;

    private UUID organisationId;

    /**
     * Catégorie de procédure (Parameter)
     * Nullable – ex : IDENTITY, TRAVEL, BUSINESS
     */
    private UUID categoryId;
}
