package com.deliacte.entity;

import com.deliacte.enums.ParameterType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "parameters",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parameter extends AbstractEntity {

    /**
     * Discriminant fonctionnel
     * (catégorie procédure, type organisation, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ParameterType type;

    /**
     * Code technique (unique par type)
     * Ex: PASSPORT, VISA, MINISTERE
     */
    @Column(name = "code", nullable = false)
    private String code;

    /**
     * Libellé affiché
     * Ex: "Passeport", "Visa", "Ministère"
     */
    @Column(name = "label", nullable = false)
    private String label;

    /**
     * Description optionnelle
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Paramètre activé ou non
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Ordre d'affichage
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
