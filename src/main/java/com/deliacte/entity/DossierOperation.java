package com.deliacte.entity;

import com.deliacte.enums.DossierOperationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dossier_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierOperation extends AbstractEntity {

    /**
     * Référence au dossier principal
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;

    /**
     * Référence à l'opération  (étape du flux)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    /**
     * Statut du dossier DANS CETTE OPÉRATION
     * PENDING = en attente de traitement
     * COMPLETED = traitement terminé avec succès
     * REJECTED = rejeté et renvoyé à une opération précédente
     * SKIPPED = étape sautée
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DossierOperationStatus status = DossierOperationStatus.PENDING;

    /**
     * Utilisateur qui a traité cette étape
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

    /**
     * Commentaire ou raison de l'action
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * Timestamp automatique : quand le dossier est arrivé à cette étape
     */
    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;

    /**
     * Timestamp : quand cette étape a été complétée
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DossierOperation)) return false;
        DossierOperation that = (DossierOperation) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
