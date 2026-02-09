package com.deliacte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "has_payment")
    @Builder.Default
    private Boolean hasPayment = false;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "is_first_operation")
    @Builder.Default
    private Boolean isFirstOperation = false;

    @Column(name = "is_last_operation")
    @Builder.Default
    private Boolean isLastOperation = false;

    @Column(name = "template_output")
    private String templateOutput;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    @JsonIgnore
    private Procedure procedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_operation_id")
    @JsonIgnore
    private TypeOperation typeOperation;

    @Column(name = "is_citizen_operation")
    @Builder.Default
    private Boolean isCitizenOperation = true; // true = gérée par le citoyen, false = par l’agent


    @ManyToMany(mappedBy = "operations", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "operation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @JsonIgnore
    @Builder.Default
    private List<ChampOperation> champOperations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "operation_entity_object",
            joinColumns = @JoinColumn(name = "operation_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_object_id")
    )
    @JsonIgnore
    private Set<EntityObject> entityObjects = new HashSet<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "operation_previous",
        joinColumns = @JoinColumn(name = "operation_id"),
        inverseJoinColumns = @JoinColumn(name = "previous_operation_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Operation> previousOperations = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "operation_next",
        joinColumns = @JoinColumn(name = "operation_id"),
        inverseJoinColumns = @JoinColumn(name = "next_operation_id")
    )
    @JsonIgnore
    @Builder.Default
    private Set<Operation> nextOperations = new HashSet<>();

    // Helper methods
    public void addChampOperation(ChampOperation champ) {
        champOperations.add(champ);
        champ.setOperation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;
        Operation operation = (Operation) o;
        return getId() != null && getId().equals(operation.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
