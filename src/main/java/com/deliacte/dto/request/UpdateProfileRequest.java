package com.deliacte.dto.request;

import com.deliacte.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
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
public class UpdateProfileRequest {

    // ── Informations de base ──────────────────────────────────────────────
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Size(max = 20)
    private String telephone;

    // ── Identité civile ───────────────────────────────────────────────────
    private String   gender;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    @Size(max = 100)
    private String placeOfBirth;

    // ── Localisation ──────────────────────────────────────────────────────
    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    // ── Statut utilisateur ────────────────────────────────────────────────
    private UserStatus userStatus;

    // ── Identification (conditionnelle selon userStatus) ──────────────────
    @Size(max = 50)
    private String idUniqueBurkinabe;      // CITOYEN

    @Size(max = 50)
    private String passportNumber;          // RESIDENT, DIPLOMATE, ETRANGER

    @Size(max = 100)
    private String nationality;             // ETRANGER, DIPLOMATE, RESIDENT

    @Size(max = 50)
    private String residencePermitNumber;   // RESIDENT

    @Size(max = 50)
    private String refugeeCardNumber;       // REFUGIE

    @Size(max = 50)
    private String diplomaticCardNumber;    // DIPLOMATE
}
 