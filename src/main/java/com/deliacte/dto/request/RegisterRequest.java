package com.deliacte.dto.request;

import com.deliacte.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // === Statut obligatoire ===
    @NotNull(message = "Le statut est obligatoire")
    private UserStatus userStatus;

    // === Informations de connexion ===
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

//    @NotBlank(message = "Le mot de passe est obligatoire")
//    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

//    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    private String telephone;

    // === Informations personnelles (optionnelles pour citoyens) ===
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String gender;
    private String address;
    private String city;
    private String country;

    // === ID Unique Burkinabè (obligatoire pour CITOYEN) ===
    private String idUniqueBurkinabe;

    // === Informations pour RESIDENT ===
    private String passportNumber;
    private String residencePermitNumber;
    private String nationality;

    // === Informations pour REFUGIE ===
    private String refugeeCardNumber;

    // === Informations pour DIPLOMATE ===
    private String diplomaticCardNumber;

    /**
     * Valide que les champs requis selon le statut sont présents
     */
    public Boolean isValidForStatus() {
        return switch (userStatus) {
            case CITOYEN -> idUniqueBurkinabe != null && !idUniqueBurkinabe.trim().isEmpty();
            case RESIDENT -> passportNumber != null && !passportNumber.trim().isEmpty() 
                          && residencePermitNumber != null && !residencePermitNumber.trim().isEmpty()
                          && nationality != null && !nationality.trim().isEmpty()
                          && firstName != null && lastName != null;
            case REFUGIE -> refugeeCardNumber != null && !refugeeCardNumber.trim().isEmpty()
                         && firstName != null && lastName != null;
            case DIPLOMATE -> diplomaticCardNumber != null && !diplomaticCardNumber.trim().isEmpty()
                           && passportNumber != null && !passportNumber.trim().isEmpty()
                           && firstName != null && lastName != null;
            case ETRANGER -> passportNumber != null && !passportNumber.trim().isEmpty()
                          && nationality != null && !nationality.trim().isEmpty()
                          && firstName != null && lastName != null;
        };
    }

    /**
     * Retourne le message d'erreur pour les champs manquants selon le statut
     */
    public String getMissingFieldsMessage() {
        return switch (userStatus) {
            case CITOYEN -> "L'ID Unique Burkinabè est obligatoire pour les citoyens";
            case RESIDENT -> "Le numéro de passeport, le permis de résidence, la nationalité, le nom et prénom sont obligatoires pour les résidents";
            case REFUGIE -> "Le numéro de carte de réfugié, le nom et prénom sont obligatoires pour les réfugiés";
            case DIPLOMATE -> "Le numéro de carte diplomatique, le numéro de passeport, le nom et prénom sont obligatoires pour les diplomates";
            case ETRANGER -> "Le numéro de passeport, la nationalité, le nom et prénom sont obligatoires pour les étrangers";
        };
    }
}
