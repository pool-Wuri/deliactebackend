package com.deliacte.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer orderIndex;
    private  String templateUrl;
    private  String procedureName;
    private  UUID procedureId;
    private Boolean active;
    private Boolean isCitizenOperation; // true = gérée par le citoyen, false = par l’agent
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProcedureSimpleResponse procedure;
    private TypeOperationResponse typeOperation;
    private List<ChampOperationResponse> champs;
}
