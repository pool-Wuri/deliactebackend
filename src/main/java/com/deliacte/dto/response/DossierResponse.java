package com.deliacte.dto.response;

import com.deliacte.enums.DossierStatus;
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
public class DossierResponse {

    private UUID id;

    private String dossierNumber;

    private UUID procedureId;
    private String procedureName;


    private UUID currentOperationId;
    private  String currentOperationName;

    private DossierStatus status;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    private String comment;

    private UUID userId;

    // Message de confirmation
    private String message;
}
