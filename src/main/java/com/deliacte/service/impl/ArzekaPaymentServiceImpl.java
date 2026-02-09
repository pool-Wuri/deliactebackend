package com.deliacte.service.impl;


import com.deliacte.dto.request.PaymentRequest;
import com.deliacte.dto.response.PaymentResponse;
import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierOperation;
import com.deliacte.entity.Payment;
import com.deliacte.repository.DossierRepository;
import com.deliacte.repository.DossierOperationRepository;
import com.deliacte.repository.PaymentRepository;
import com.deliacte.service.ArzekaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArzekaPaymentServiceImpl implements ArzekaPaymentService {

    private final PaymentRepository paymentRepository;
    private final DossierRepository dossierRepository;
    private final DossierOperationRepository dossierOperationRepository;

    @Value("${arzeka.merchant.id:174}")
    private String merchantId;

    @Value("${arzeka.token:YOUR_SECURED_ACCESS_TOKEN}")
    private String securedAccessToken;

    @Value("${arzeka.base.url:https://pgw-test.fasoarzeka.bf}")
    private String baseUrl;

    @Value("${app.callback.url:http://localhost:8090/api/payments/callback}")
    private String callbackUrl;

    /**
     * Initier un paiement sur un dossier (hybride).
     */
    @Override
    public String initiatePaymentOnDossier(String dossierId, PaymentRequest dto) {
        Dossier dossier = dossierRepository.findByDossierNumber(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier not found"));

        // Vérifier qu’aucun paiement n’existe déjà sur une opération
        if (paymentRepository.existsByDossierOperation_Dossier(dossier)) {
            throw new IllegalStateException("Paiement déjà effectué sur une opération.");
        }

        // Vérifier qu’aucun paiement n’existe déjà sur le dossier
        if (paymentRepository.existsByDossier(dossier)) {
            throw new IllegalStateException("Paiement déjà effectué sur ce dossier.");
        }

        String mappedOrderId = generateMappedOrderId();

        String url = buildPaymentUrl(dto.getMsisdn(), dto.getAmount(), mappedOrderId);

        Payment payment = Payment.builder()
                .dossier(dossier)
                .msisdn(dto.getMsisdn())
                .amount(dto.getAmount())
                .merchantId(merchantId)
                .mappedOrderId(mappedOrderId)
                .status("PENDING")
                .build();

        paymentRepository.save(payment);

        return url;
    }

    /**
     * Initier un paiement sur une opération (hybride).
     */
    @Override
    public String initiatePaymentOnOperation(UUID operationId, PaymentRequest dto) {
        DossierOperation operation = dossierOperationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found"));

        // Vérifier qu’aucun paiement n’existe déjà sur le dossier global
        if (paymentRepository.existsByDossier(operation.getDossier())) {
            throw new IllegalStateException("Paiement déjà effectué sur le dossier.");
        }

        // Vérifier qu’aucun paiement n’existe déjà sur cette opération
        if (paymentRepository.existsByDossierOperation(operation)) {
            throw new IllegalStateException("Paiement déjà effectué sur cette opération.");
        }

        String mappedOrderId = generateMappedOrderId();

        String url = buildPaymentUrl(dto.getMsisdn(), dto.getAmount(), mappedOrderId);

        Payment payment = Payment.builder()
                .dossierOperation(operation)
                .msisdn(dto.getMsisdn())
                .amount(dto.getAmount())
                .merchantId(merchantId)
                .mappedOrderId(mappedOrderId)
                .status("PENDING")
                .build();

        paymentRepository.save(payment);

        return url;
    }

    /**
     * Vérifie le statut d’un paiement auprès d’ARZEKA et met à jour la base.
     */
    @Override
    public PaymentResponse checkPaymentStatus(String mappedOrderId) {
        String checkUrl = baseUrl + "/AvepayPaymentGatewayUI/avepay-payment/app/getThirdPartyMapInfo?mappedOrderId=" + mappedOrderId;

        RestTemplate restTemplate = new RestTemplate();
        PaymentResponse response = restTemplate.postForObject(checkUrl, null, PaymentResponse.class);

        Payment payment = paymentRepository.findByMappedOrderId(mappedOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(response.getStatus());
        payment.setPaymentInfo(response.getPaymentInfo());
        payment.setPaymentMethod(response.getPaymentMethod());
        payment.setAccountInfo(response.getAccountInfo());
        payment.setTransId(response.getTransId());
        payment.setTransTimeStamp(response.getTransTimeStamp());
        payment.setOrderId(response.getOrderId());
        payment.setThirdPartyModule(response.getThirdPartyModule());
        payment.setThirdPartyTransId(response.getThirdPartyTransId());
        payment.setThirdPartyRedirectUrl(response.getThirdPartyRedirectUrl());
        payment.setAmount(response.getAmount());
        payment.setCreatedAt(response.getCreatedAt());

        // Notifier que c’est payé si tout est OK
        if ("SUCCESS".equalsIgnoreCase(response.getStatus()) &&
                "COMPLETED".equalsIgnoreCase(response.getPaymentInfo())) {
            // Ici tu peux notifier le dossier ou l’opération comme payé
            // Exemple : changer un flag dans Dossier ou DossierOperation
        }

        paymentRepository.save(payment);

        return response;
    }

    private String generateMappedOrderId() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        return "DELIACTE-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 5);
    }

    @Override
    public PaymentResponse updatePaymentFromCallback(String status,
                                                     String transTimeStamp,
                                                     String paymentInfo,
                                                     String paymentMethod,
                                                     String accountInfo,
                                                     String transId,
                                                     String paymentRequestId) {

        // 1. Retrouver le paiement en base grâce au mappedOrderId (paymentRequestId)
        Payment payment = paymentRepository.findByMappedOrderId(paymentRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for requestId: " + paymentRequestId));

        // 2. Mettre à jour les champs du paiement
        payment.setStatus(status);
        payment.setPaymentInfo(paymentInfo);
        payment.setPaymentMethod(paymentMethod);
        payment.setAccountInfo(accountInfo);
        payment.setTransId(transId);

        try {
            payment.setTransTimeStamp(java.time.LocalDateTime.parse(transTimeStamp));
        } catch (Exception e) {
            // Si le format n’est pas ISO, tu peux parser autrement ou ignorer
            payment.setTransTimeStamp(java.time.LocalDateTime.now());
        }

        // 3. Si tout est OK, notifier que c’est payé
        if ("SUCCESS".equalsIgnoreCase(status) && "COMPLETED".equalsIgnoreCase(paymentInfo)) {
            // Exemple : tu peux ajouter un flag isPaid dans Dossier ou DossierOperation
            if (payment.getDossier() != null) {
                payment.getDossier().setStatus(com.deliacte.enums.DossierStatus.COMPLETED);
            }
            if (payment.getDossierOperation() != null) {
                payment.getDossierOperation().setStatus(com.deliacte.enums.DossierOperationStatus.COMPLETED);
            }
        }

        paymentRepository.save(payment);

        // 4. Construire la réponse DTO
        return PaymentResponse.builder()
                .status(payment.getStatus())
                .transTimeStamp(payment.getTransTimeStamp())
                .paymentInfo(payment.getPaymentInfo())
                .paymentMethod(payment.getPaymentMethod())
                .accountInfo(payment.getAccountInfo())
                .transId(payment.getTransId())
                .paymentRequestId(paymentRequestId)
                .amount(payment.getAmount())
                .merchantId(payment.getMerchantId())
                .mappedOrderId(payment.getMappedOrderId())
                .build();
    }





    private String buildPaymentUrl(String msisdn, BigDecimal amount, String mappedOrderId) {
        String encodedCallback = Base64.getEncoder().encodeToString(callbackUrl.getBytes());
        String encodedUpdate = Base64.getEncoder().encodeToString(callbackUrl.getBytes());

        return baseUrl + "/AvepayPaymentGatewayUI/avepay-payment/app/validorder?"
                + "amount=" + amount
                + "&msisdn=" + msisdn
                + "&merchantid=" + merchantId
                + "&securedAccessToken=" + securedAccessToken
                + "&mappedOrderId=" + mappedOrderId
                + "&linkForUpdateStatus=" + encodedUpdate
                + "&linkBackToCallingWebsite=" + encodedCallback;
    }
}
