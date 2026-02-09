package com.deliacte.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Ancien mot de passe requis")
    private String oldPassword;

    @NotBlank(message = "Nouveau mot de passe requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String newPassword;

    @NotBlank(message = "Confirmation du mot de passe requis")
    private String confirmPassword;
}
