package com.deliacte.dto.response;

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
public class OperationSimpleResponse {

    private UUID id;
    private String code;
    private String name;
    private String verbeOperation;
    private Integer numeroOrdre;
    private Boolean isActive;
    private Boolean hasPayment;
    private BigDecimal prix;
    private Boolean isFirstOperation;
    private Boolean isLastOperation;
}
