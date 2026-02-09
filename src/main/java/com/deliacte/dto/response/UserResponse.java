package com.deliacte.dto.response;

import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    
    // Statut de l'utilisateur
    private UserStatus userStatus;
    private String userStatusLabel;
    
    // Rôle
    private UserRole role;
    
    // Informations d'identification
    private String idUniqueBurkinabe;
    private String passportNumber;
    private String nationality;
    private String residencePermitNumber;
    private String refugeeCardNumber;
    private String diplomaticCardNumber;
    
    // Informations personnelles
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String gender;
    private String address;
    private String city;
    private String country;
    
    // État du compte
    private Boolean enabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean accountNonLocked;
    
    // Statistiques
    private Integer connectionCount;
    private LocalDateTime lastLoginAt;
    
    // Photo
    private String profilePhotoUrl;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relations
    private Set<OrganisationSimpleResponse> organisations;
    private Set<ProcedureSimpleResponse> procedures;
    private Set<OperationSimpleResponse> operations;

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
