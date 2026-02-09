package com.deliacte.dto.request;

import com.deliacte.enums.EnumInputFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ChampOperationRequest {

    @Size(max = 100, message = "Le code ne doit pas dépasser 100 caractères")
    private String code;

    @NotBlank(message = "Le label est obligatoire")
    @Size(min = 1, max = 255, message = "Le label doit contenir entre 1 et 255 caractères")
    private String label;

    @Size(max = 255, message = "Le placeholder ne doit pas dépasser 255 caractères")
    private String placeholder;

    @Size(max = 500, message = "Le texte d'aide ne doit pas dépasser 500 caractères")
    private String helpText;

    private EnumInputFieldType fieldType;

    private Boolean required;

    private Integer minLength;

    private Integer maxLength;

    private String pattern;

    private String defaultValue;

    private Integer orderIndex;

    private Boolean active;

    private UUID operationId;

    private UUID entityObjectId;

    private List<OptionChampOperationRequest> options;
}
