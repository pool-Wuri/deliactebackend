package com.deliacte.dto.response;

import com.deliacte.enums.ProcedureStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcedureSimpleResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private BigDecimal fee;
    private Boolean hasPayment;
    private ProcedureStatus status;
    private Boolean isPublic;
    private String organisationName;
}
