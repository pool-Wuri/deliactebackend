package com.deliacte.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentRequest {

    /** Montant effectivement reçu */
    private BigDecimal amount;

    /** Référence du reçu / numéro de quittance */
    private String reference;

    /** Mode de paiement : ESPECES, VIREMENT_BANCAIRE, CHEQUE, AUTRE */
    private String paymentMethod;

    /** Observations libres de l'agent */
    private String note;
}
