package com.deliacte.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyDossierResponseDto {

    private UUID dossierId;
    private String dossierNumber;

    private String procedureName;
    private String currentOperationName;

    private String status;
    private LocalDateTime submittedAt;
}
