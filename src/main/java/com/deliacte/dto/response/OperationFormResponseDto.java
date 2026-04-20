package com.deliacte.dto.response;

import lombok.*;

import java.math.BigDecimal;
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




    private String rejectionComment;

    /** Statut de la DossierOperation pour ce dossier+opération (null si non applicable) */
    private String dossierOperationStatus;

    /** Paiement requis sur cette opération spécifique */
    private Boolean operationHasPayment;
    private BigDecimal operationFee;

    /** Paiement requis sur la procédure entière */
    private Boolean procedureHasPayment;
    private BigDecimal procedureFee;

    // Champs par entité
    private List<EntityObjectWithChampOperationDto> entities;

    // Champs sans entité (globaux à l'opération)
    private List<ChampOperationResponseDto> otherChampOperations;
}
