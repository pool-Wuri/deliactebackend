package com.deliacte.dto.response;

import com.deliacte.enums.DossierStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DossierTimelineResponse {

    private String dossierNumber;
    private String procedureName;
    private String organisationName;
    private DossierStatus dossierStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    /** Toutes les opérations de la procédure, ordonnées par orderIndex */
    private List<DossierOperationStepResponse> steps;
}
