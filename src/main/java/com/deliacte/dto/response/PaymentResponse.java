package com.deliacte.dto.response;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String status;              // SUCCESS / FAILED
    private LocalDateTime transTimeStamp; // Date/heure de la transaction
    private String paymentInfo;         // COMPLETED / INCOMPLETE / PENDING
    private String paymentMethod;       // ORANGEMONEY, MOOVMONEY, etc.
    private String accountInfo;         // Numéro de compte utilisé
    private String transId;             // Identifiant transaction Arzeka
    private String paymentRequestId;    // mappedOrderId que tu as envoyé

    // Champs supplémentaires de l’API Arzeka (check payment)
    private String orderId;             // order_id interne Arzeka
    private String merchantId;          // Identifiant du marchand
    private String thirdPartyModule;    // Module opérateur (Moov, Orange…)
    private String mappedOrderId; // référence transactionnelle
    private String thirdPartyTransId;   // ID transaction côté opérateur
    private String thirdPartyRedirectUrl; // URL de redirection
    private BigDecimal amount;          // Montant payé
    private LocalDateTime createdAt;    // Date de création
}
