package com.deliacte.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionChampOperationRequest {

    @NotBlank(message = "Le label est obligatoire")
    @Size(min = 1, max = 255, message = "Le label doit contenir entre 1 et 255 caractères")
    private String label;

//    @NotBlank(message = "La valeur est obligatoire")
//    @Size(min = 1, max = 255, message = "La valeur doit contenir entre 1 et 255 caractères")
    private String value;

    private Integer ordre;

    private  Integer orderIndex;

    private Boolean isDefault;

    private UUID champOperationId;
}
