package com.deliacte.entity;

import com.deliacte.enums.ProcedureStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "procedures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedure extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "processing_time")
    private String processingTime;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "has_payment")
    @Builder.Default
    private Boolean hasPayment = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ProcedureStatus status = ProcedureStatus.DRAFT;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    // Statuts d'utilisateurs autorisés (stockés comme chaîne séparée par des virgules)
    // Ex: "CITOYEN,RESIDENT,REFUGIE"
    @Column(name = "allowed_user_statuses")
    private String allowedUserStatuses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    @JsonIgnore
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Parameter category; // type = PROCEDURE_CATEGORY


    @OneToMany(mappedBy = "procedure", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @JsonIgnore
    @Builder.Default
    private List<Operation> operations = new ArrayList<>();

    @ManyToMany(mappedBy = "procedures", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "procedure", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Dossier> dossiers = new ArrayList<>();

    // Helper methods
    public void addOperation(Operation operation) {
        operations.add(operation);
        operation.setProcedure(this);
    }

    public void removeOperation(Operation operation) {
        operations.remove(operation);
        operation.setProcedure(null);
    }

    /**
     * Vérifie si un statut d'utilisateur est autorisé pour cette procédure
     */
    public Boolean isUserStatusAllowed(String userStatus) {
        if (allowedUserStatuses == null || allowedUserStatuses.isEmpty()) {
            return true; // Si pas de restriction, tous sont autorisés
        }
        return allowedUserStatuses.contains(userStatus);
    }

    /**
     * Retourne la liste des statuts autorisés
     */
    public List<String> getAllowedUserStatusesList() {
        if (allowedUserStatuses == null || allowedUserStatuses.isEmpty()) {
            return List.of();
        }
        return List.of(allowedUserStatuses.split(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Procedure)) return false;
        Procedure procedure = (Procedure) o;
        return getId() != null && getId().equals(procedure.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
