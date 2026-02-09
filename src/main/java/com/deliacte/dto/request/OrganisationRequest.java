package com.deliacte.dto.request;

import jakarta.validation.constraints.Email;
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
public class OrganisationRequest {

    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    @Size(min = 2, max = 255, message = "Le nom doit contenir entre 2 et 255 caractères")
    private String name;

    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    private String code;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    private String address;

    private String telephone;

    @Email(message = "L'email doit être valide")
    private String email;

    private String website;

    private String logoUrl;

    private UUID parentId;
    /**
     * Type d'organisation (Parameter)
     * Nullable – ex : MINISTRY, AGENCY, ONG
     */
    private UUID organisationTypeId;
}
