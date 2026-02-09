package com.deliacte.entity;

import com.deliacte.enums.EnumInputFieldType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "champ_operation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampOperation extends AbstractEntity {

    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "placeholder")
    private String placeholder;

    @Column(name = "help_text", columnDefinition = "TEXT")
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type")
    private EnumInputFieldType fieldType = EnumInputFieldType.TEXT;

    @Column(name = "required")
    @Builder.Default
    private Boolean required = false;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "pattern")
    private String pattern;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;
    @Column(name = "order_operation")
    @Builder.Default
    private Integer order = 1;

    @Column(name = "order_entity")
    @Builder.Default
    private Integer orderIndex = 1;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    @JsonIgnore
    private Operation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_object_id")
    @JsonIgnore
    private EntityObject entityObject;

    @OneToMany(mappedBy = "champOperation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OptionChampOperation> options = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChampOperation)) return false;
        ChampOperation that = (ChampOperation) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ChampOperation{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", label='" + label + '\'' +
                ", fieldType=" + fieldType +
                '}';
    }
}
