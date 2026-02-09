package com.deliacte.entity;

import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AbstractEntity implements UserDetails {

    // === Informations de base ===
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telephone", unique = true)
    private String telephone;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    // === Statut de l'utilisateur (Citoyen, Résident, Réfugié, Diplomate, Étranger) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    @Builder.Default
    private UserStatus userStatus = UserStatus.CITOYEN;

    // === ID Unique Burkinabè (pour les citoyens) ===
    @Column(name = "id_unique_burkinabe", unique = true)
    private String idUniqueBurkinabe;

    // === Informations d'identification pour non-citoyens ===
    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "residence_permit_number")
    private String residencePermitNumber;

    @Column(name = "refugee_card_number")
    private String refugeeCardNumber;

    @Column(name = "diplomatic_card_number")
    private String diplomaticCardNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    // === Rôle et permissions ===
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.CITOYEN;

    // === État du compte ===
    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "account_non_locked")
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "phone_verified")
    @Builder.Default
    private boolean phoneVerified = false;

    // === Statistiques de connexion ===
    @Column(name = "connection_count")
    @Builder.Default
    private Integer connectionCount = 0;

    @Column(name = "connection_failure_count")
    @Builder.Default
    private Integer connectionFailureCount = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    // === Tokens de sécurité ===
    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    // === Photo de profil ===
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    // === Relations ===
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_organisation",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "organisation_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Organisation> organisations = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_procedure",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "procedure_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Procedure> procedures = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_operation",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "operation_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Operation> operations = new HashSet<>();

    // === UserDetails Implementation ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // === Helper Methods ===
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public void incrementConnectionCount() {
        this.connectionCount = (this.connectionCount == null ? 0 : this.connectionCount) + 1;
    }

    public void incrementFailedLoginAttempts() {
        this.connectionFailureCount = (this.connectionFailureCount == null ? 0 : this.connectionFailureCount) + 1;
    }

    public void resetFailedLoginAttempts() {
        this.connectionFailureCount = 0;
    }

    public void lockAccount() {
        this.accountNonLocked = false;
    }

    public void unlockAccount() {
        this.accountNonLocked = true;
        this.connectionFailureCount = 0;
    }

    /**
     * Vérifie si l'utilisateur est un citoyen burkinabè
     */
    public Boolean isCitoyen() {
        return this.userStatus == UserStatus.CITOYEN;
    }

    /**
     * Vérifie si l'utilisateur a fourni les informations d'identification requises selon son statut
     */
    public Boolean hasRequiredIdentification() {
        return switch (this.userStatus) {
            case CITOYEN -> idUniqueBurkinabe != null && !idUniqueBurkinabe.isEmpty();
            case RESIDENT -> passportNumber != null && residencePermitNumber != null;
            case REFUGIE -> refugeeCardNumber != null;
            case DIPLOMATE -> diplomaticCardNumber != null && passportNumber != null;
            case ETRANGER -> passportNumber != null && nationality != null;
        };
    }
}
