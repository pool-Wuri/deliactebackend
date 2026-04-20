package com.deliacte.dto.response;

import com.deliacte.enums.DossierStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierListResponse {

    private UUID id;
    private String dossierNumber;

    private UUID procedureId;
    private String procedureName;
    private  String citoyenName;
    private DossierStatus status;

    private LocalDateTime submittedAt;

    private UUID currentOperationId;
    private String currentOperationName;
    private Boolean isCitizenCurrentOperation;
}
