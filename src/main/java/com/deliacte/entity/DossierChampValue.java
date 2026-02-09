package com.deliacte.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dossier_champ_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierChampValue extends AbstractEntity {

    // La valeur du champ
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    // Chemin du fichier si c'est un upload
    @Column(name = "file_path")
    private String filePath;

    // Lien vers l'historique (chaque soumission crée ses propres valeurs)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_historique_id", nullable = false)
    private DossierHistorique dossierHistorique;

    // Le champ de l'opération auquel cette valeur correspond
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champ_operation_id", nullable = false)
    private ChampOperation champOperation;

    // VERSION : Numéro de version de cette valeur (1, 2, 3, ...)
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    // ACTIVE : Indique si cette valeur est la version courante et active
    // true = c'est la dernière version, elle est utilisée dans le dossier
    // false = c'est une ancienne version, elle est conservée pour l'audit
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DossierChampValue)) return false;
        DossierChampValue that = (DossierChampValue) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
