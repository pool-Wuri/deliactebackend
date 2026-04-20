package com.deliacte.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentStatusResponse {

    private UUID paymentId;

    /** DOSSIER = paiement lié à la procédure entière, OPERATION = paiement lié à une étape */
    private String scope;

    private String dossierNumber;

    /** ID de la DossierOperation si scope=OPERATION */
    private UUID dossierOperationId;

    /** Nom de l'opération si scope=OPERATION */
    private String operationName;

    private boolean paymentRequired;
    private boolean isPaid;

    /** Montant attendu (fee de la procédure ou de l'opération) */
    private BigDecimal requiredAmount;

    /** Montant réellement enregistré */
    private BigDecimal paidAmount;

    private String reference;
    private String paymentMethod;
    private String note;
    private String validatedByName;
    private LocalDateTime paidAt;
}
