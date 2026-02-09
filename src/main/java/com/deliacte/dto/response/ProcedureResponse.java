package com.deliacte.dto.response;

import com.deliacte.enums.ProcedureStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcedureResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private String requirements;
    private String processingTime;
    private BigDecimal fee;
    private Boolean hasPayment;
    private ProcedureStatus status;
    private Boolean isPublic;
    private String allowedUserStatuses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ParameterSimpleResponse category;
    private OrganisationSimpleResponse organisation;
    private List<OperationSimpleResponse> operations;
    private Integer dossierCount;

}
