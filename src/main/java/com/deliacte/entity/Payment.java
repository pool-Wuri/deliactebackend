package com.deliacte.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AbstractEntity {

    @Column(name = "msisdn")
    private String msisdn; // numéro mobile

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "mapped_order_id", unique = true)
    private String mappedOrderId; // référence transactionnelle

    @Column(name = "status")
    private String status; // SUCCESS, FAILED, PENDING

    @Column(name = "payment_info")
    private String paymentInfo; // COMPLETED, INCOMPLETE, PENDING

    @Column(name = "payment_method")
    private String paymentMethod; // ORANGEMONEY, MOOVMONEY, etc.

    @Column(name = "account_info")
    private String accountInfo;

    @Column(name = "trans_id")
    private String transId; // ID transaction Arzeka

    @Column(name = "trans_timestamp")
    private LocalDateTime transTimeStamp;

    @Column(name = "order_id")
    private String orderId; // ID interne Arzeka

    @Column(name = "third_party_module")
    private String thirdPartyModule;

    @Column(name = "third_party_trans_id")
    private String thirdPartyTransId;

    @Column(name = "third_party_redirect_url")
    private String thirdPartyRedirectUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relations hybrides
    @ManyToOne(fetch = FetchType.LAZY)
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    private DossierOperation dossierOperation;
}
