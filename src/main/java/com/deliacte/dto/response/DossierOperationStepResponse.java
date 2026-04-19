package com.deliacte.dto.response;

import com.deliacte.enums.DossierOperationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DossierOperationStepResponse {

    private UUID operationId;
    private String operationName;
    private int orderIndex;
    private boolean isCitizenOperation;
    private String typeOperationLibelle;

    /** null = opération non encore atteinte */
    private DossierOperationStatus stepStatus;
    private String processedByName;
    private String comment;
    private LocalDateTime receivedAt;
    private LocalDateTime completedAt;

    private boolean isCurrent;
    private boolean isFirst;
    private boolean isLast;
}
