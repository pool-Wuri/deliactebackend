package com.deliacte.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private String msisdn;              // Numéro mobile
    private BigDecimal amount;          // Montant à payer
    private String merchantId;          // Identifiant du marchand
    private String securedAccessToken;  // Token d’authentification
    private String mappedOrderId;       // Référence unique de la transaction
    private String linkForUpdateStatus; // URL encodée Base64 pour mise à jour
    private String linkBackToCallingWebsite; // URL encodée Base64 pour callback
}
