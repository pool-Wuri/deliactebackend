package com.deliacte.entity;

import com.deliacte.enums.DossierActionType;
import com.deliacte.enums.DossierStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossier_historiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierHistorique extends AbstractEntity {

    // Référence au dossier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonIgnore
    private Dossier dossier;

    // Utilisateur qui a effectué cette action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // Statut du dossier après cette action
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DossierStatus status;

    // Type d'action effectuée (SUBMIT, REJECT, CREATE, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private DossierActionType actionType;

    // Commentaire ou raison de l'action
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    // Timestamp automatique de l'action
    @CreationTimestamp
    @Column(name = "action_date", updatable = false)
    private LocalDateTime actionDate;

    // Opération source (d'où venait le dossier)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_operation_id")
    @JsonIgnore
    private Operation fromOperation;

    // Opération cible (où va le dossier après cette action)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dossier_historique_target_operations",
            joinColumns = @JoinColumn(name = "historique_id"),
            inverseJoinColumns = @JoinColumn(name = "operation_id")
    )
    private List<Operation> targetOperations = new ArrayList<>();

    // Les valeurs des champs soumises lors de CETTE action spécifique
    // C'est la clé pour la traçabilité : chaque historique contient une "photographie" des données
    @OneToMany(mappedBy = "dossierHistorique", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<DossierChampValue> champValues = new ArrayList<>();

    // Helper method
    public void addChampValue(DossierChampValue champValue) {
        champValues.add(champValue);
        champValue.setDossierHistorique(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DossierHistorique)) return false;
        DossierHistorique that = (DossierHistorique) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
