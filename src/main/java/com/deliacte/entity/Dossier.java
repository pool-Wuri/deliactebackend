package com.deliacte.entity;

import com.deliacte.enums.DossierStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dossier extends AbstractEntity {

    @Column(name = "dossier_number", unique = true, nullable = false)
    private String dossierNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private DossierStatus status = DossierStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "has_payment")
    private Boolean hasPayment;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "payment_status")
    private String paymentStatus; // ex: PENDING, PAID, FAILED




    // La liste des étapes du parcours du dossier
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DossierOperation> operationSteps = new ArrayList<>();

    // L'historique des valeurs soumises (peut être conservé pour l'audit)
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("actionDate DESC")
    @Builder.Default
    private List<DossierHistorique> historiques = new ArrayList<>();

    // Helper methods
    public void addOperationStep(DossierOperation step) {
        operationSteps.add(step);
        step.setDossier(this);
    }

    public void addHistorique(DossierHistorique historique) {
        historiques.add(historique);
        historique.setDossier(this);
    }
}
