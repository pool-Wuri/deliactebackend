package com.deliacte.dto.request;

import com.deliacte.enums.ParameterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterRequest {

    /**
     * Type de paramètre
     * (PROCEDURE_CATEGORY, ORGANISATION_TYPE, etc.)
     */
    @NotNull(message = "Le type de paramètre est obligatoire")
    private ParameterType type;

    /**
     * Code technique unique par type
     */
    @NotBlank(message = "Le code est obligatoire")
    @Size(min = 2, max = 50, message = "Le code doit contenir entre 2 et 50 caractères")
    private String code;

    /**
     * Libellé affiché
     */
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(min = 2, max = 255, message = "Le libellé doit contenir entre 2 et 255 caractères")
    private String label;

    /**
     * Description optionnelle
     */
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    /**
     * Activation / désactivation
     * (optionnel, true par défaut)
     */
    private Boolean isActive;

    /**
     * Ordre d'affichage
     */
    private Integer displayOrder;
}
