package com.deliacte.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationFormResponseDto {


    private UUID currentOperationId;
    private  String operationName;
    private  String procedureName;
    private  UUID procedureId;
    private  String numeroDossier ;




    // Champs par entité
    private List<EntityObjectWithChampOperationDto> entities;

    // Champs sans entité (globaux à l'opération)
    private List<ChampOperationResponseDto> otherChampOperations;
}
