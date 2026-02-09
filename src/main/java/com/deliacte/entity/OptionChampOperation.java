package com.deliacte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_champ_operation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionChampOperation extends AbstractEntity {

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "ordre")
    private Integer ordre;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champ_operation_id", nullable = false)
    @JsonIgnore
    private ChampOperation champOperation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionChampOperation)) return false;
        OptionChampOperation that = (OptionChampOperation) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
