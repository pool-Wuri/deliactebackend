package com.deliacte.service.impl;

import com.deliacte.dto.request.PaymentRequest;
import com.deliacte.dto.response.PaymentResponse;
import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierOperation;
import com.deliacte.entity.Payment;
import com.deliacte.repository.DossierOperationRepository;
import com.deliacte.repository.DossierRepository;
import com.deliacte.repository.PaymentRepository;
import com.deliacte.service.ArzekaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArzekaPaymentServiceImpl implements ArzekaPaymentService {

    private final PaymentRepository paymentRepository;
    private final DossierRepository dossierRepository;
    private final DossierOperationRepository dossierOperationRepository;

    @Value("${arzeka.merchant.id:174}")
    private String merchantId;

    @Value("${arzeka.token:eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiI1UzNaOVQ4MzAxIiwiaWF0IjoxNzIwMDA4ODI5LCJzdWIiOiIyMjYwMDAwMDAxNiIsImlzcyI6ImFyemVrYSIsIlBBWUxPQUQiOiJhY2Nlc3NfdG9rZW4iLCJleHAiOjE3ODMwODA4Mjl9.BZRA8xd0nh_H8JfyTteXTrtmjPT2hWKmTsZavRYQSJoDFW97ik5rR5SyhLmTr9nzmDQ2MHA24qGbtgH9djK4Lg}")
    private String securedAccessToken;

    @Value("${arzeka.base.url:https://pgw-test.fasoarzeka.bf}")
    private String baseUrl;

    /** URL backend appelée par Arzeka côté serveur (linkForUpdateStatus) */
    @Value("${app.arzeka.update-url:http://localhost:8090/v1/payments/arzeka/return}")
    private String updateStatusUrl;

    /** URL backend de retour citoyen après paiement Arzeka (linkBackToCallingWebsite) */
    @Value("${app.arzeka.return-url:http://localhost:8090/v1/payments/arzeka/return}")
    private String returnUrl;

    /** URL frontend pour rediriger le citoyen après callback */
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // Admin / hybride
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String initiatePaymentOnDossier(String dossierId, PaymentRequest dto) {
        Dossier dossier = dossierRepository.findByDossierNumber(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier not found"));

        if (paymentRepository.existsByDossierOperation_Dossier(dossier)) {
            throw new IllegalStateException("Paiement déjà effectué sur une opération.");
        }
        if (paymentRepository.existsByDossier(dossier)) {
            throw new IllegalStateException("Paiement déjà effectué sur ce dossier.");
        }

        String mappedOrderId = generateMappedOrderId();
        String url = buildPaymentUrl(dto.getMsisdn(), dto.getAmount(), mappedOrderId, returnUrl);

        paymentRepository.save(Payment.builder()
                .dossier(dossier)
                .msisdn(dto.getMsisdn())
                .amount(dto.getAmount())
                .merchantId(merchantId)
                .mappedOrderId(mappedOrderId)
                .status("PENDING")
                .thirdPartyModule("ARZEKA")
                .createdAt(LocalDateTime.now())
                .build());

        return url;
    }

    @Override
    public String initiatePaymentOnOperation(UUID operationId, PaymentRequest dto) {
        DossierOperation operation = dossierOperationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found"));

        if (paymentRepository.existsByDossier(operation.getDossier())) {
            throw new IllegalStateException("Paiement déjà effectué sur le dossier.");
        }
        if (paymentRepository.existsByDossierOperation(operation)) {
            throw new IllegalStateException("Paiement déjà effectué sur cette opération.");
        }

        String mappedOrderId = generateMappedOrderId();
        String url = buildPaymentUrl(dto.getMsisdn(), dto.getAmount(), mappedOrderId, returnUrl);

        paymentRepository.save(Payment.builder()
                .dossierOperation(operation)
                .msisdn(dto.getMsisdn())
                .amount(dto.getAmount())
                .merchantId(merchantId)
                .mappedOrderId(mappedOrderId)
                .status("PENDING")
                .thirdPartyModule("ARZEKA")
                .createdAt(LocalDateTime.now())
                .build());

        return url;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Citoyen : montant automatique depuis le fee configuré
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String initiateCitizenPaymentForDossier(String dossierNumber, String msisdn) {
        Dossier dossier = dossierRepository.findByDossierNumber(dossierNumber)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable : " + dossierNumber));

        BigDecimal amount = dossier.getProcedure().getFee();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Aucun montant configuré pour cette procédure.");
        }

        // Réutiliser un paiement PENDING existant ou en créer un nouveau
        Payment existing = paymentRepository.findByDossier(dossier).orElse(null);
        if (existing != null && "SUCCESS".equalsIgnoreCase(existing.getStatus())
                && "COMPLETED".equalsIgnoreCase(existing.getPaymentInfo())) {
            throw new IllegalStateException("Le paiement pour ce dossier est déjà complété.");
        }

        String mappedOrderId = (existing != null && "PENDING".equalsIgnoreCase(existing.getStatus()))
                ? existing.getMappedOrderId()
                : generateMappedOrderId();

        if (existing == null || !"PENDING".equalsIgnoreCase(existing.getStatus())) {
            paymentRepository.save(Payment.builder()
                    .dossier(dossier)
                    .msisdn(msisdn)
                    .amount(amount)
                    .merchantId(merchantId)
                    .mappedOrderId(mappedOrderId)
                    .status("PENDING")
                    .thirdPartyModule("ARZEKA")
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        log.info("Initiation paiement citoyen dossier {} — montant {} FCFA", dossierNumber, amount);
        return buildPaymentUrl(msisdn, amount, mappedOrderId, returnUrl + "?dossierNumber=" + dossierNumber);
    }

    @Override
    public String initiateCitizenPaymentForOperation(String dossierNumber, UUID dossierOperationId, String msisdn) {
        Dossier dossier = dossierRepository.findByDossierNumber(dossierNumber)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable : " + dossierNumber));

        DossierOperation dossierOp = dossierOperationRepository.findById(dossierOperationId)
                .orElseThrow(() -> new IllegalArgumentException("Étape introuvable : " + dossierOperationId));

        if (!dossierOp.getDossier().getId().equals(dossier.getId())) {
            throw new IllegalArgumentException("Cette étape n'appartient pas au dossier indiqué");
        }

        BigDecimal amount = dossierOp.getOperation().getFee();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Aucun montant configuré pour cette étape.");
        }

        Payment existing = paymentRepository.findByDossierOperation(dossierOp).orElse(null);
        if (existing != null && "SUCCESS".equalsIgnoreCase(existing.getStatus())
                && "COMPLETED".equalsIgnoreCase(existing.getPaymentInfo())) {
            throw new IllegalStateException("Le paiement pour cette étape est déjà complété.");
        }

        String mappedOrderId = (existing != null && "PENDING".equalsIgnoreCase(existing.getStatus()))
                ? existing.getMappedOrderId()
                : generateMappedOrderId();

        if (existing == null || !"PENDING".equalsIgnoreCase(existing.getStatus())) {
            paymentRepository.save(Payment.builder()
                    .dossierOperation(dossierOp)
                    .msisdn(msisdn)
                    .amount(amount)
                    .merchantId(merchantId)
                    .mappedOrderId(mappedOrderId)
                    .status("PENDING")
                    .thirdPartyModule("ARZEKA")
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        log.info("Initiation paiement citoyen étape {} dossier {} — montant {} FCFA",
                dossierOperationId, dossierNumber, amount);
        return buildPaymentUrl(msisdn, amount, mappedOrderId,
                returnUrl + "?dossierNumber=" + dossierNumber + "&dossierOperationId=" + dossierOperationId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Check & Callback
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public PaymentResponse checkPaymentStatus(String mappedOrderId) {
        String checkUrl = baseUrl
                + "/AvepayPaymentGatewayUI/avepay-payment/app/getThirdPartyMapInfo?mappedOrderId="
                + mappedOrderId;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + securedAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<PaymentResponse> resp = restTemplate.exchange(
                checkUrl, HttpMethod.POST,
                new HttpEntity<>(headers),
                PaymentResponse.class);

        PaymentResponse response = resp.getBody();
        if (response == null) return PaymentResponse.builder().status("UNKNOWN").build();

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
        payment.setAmount(response.getAmount());

        paymentRepository.save(payment);
        return response;
    }

    @Override
    public PaymentResponse updatePaymentFromCallback(String status, String transTimeStamp,
                                                     String paymentInfo, String paymentMethod,
                                                     String accountInfo, String transId,
                                                     String paymentRequestId) {

        Payment payment = paymentRepository.findByMappedOrderId(paymentRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found for requestId: " + paymentRequestId));

        payment.setStatus(status);
        payment.setPaymentInfo(paymentInfo);
        payment.setPaymentMethod(paymentMethod);
        payment.setAccountInfo(accountInfo);
        payment.setTransId(transId);

        try {
            payment.setTransTimeStamp(LocalDateTime.parse(transTimeStamp));
        } catch (Exception e) {
            payment.setTransTimeStamp(LocalDateTime.now());
        }

        if ("SUCCESS".equalsIgnoreCase(status) && "COMPLETED".equalsIgnoreCase(paymentInfo)) {
            log.info("Paiement ARZEKA confirmé — mappedOrderId={}", paymentRequestId);
        }

        paymentRepository.save(payment);

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

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String generateMappedOrderId() {
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String rand = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "DELIACTE-" + ts + "-" + rand;
    }

    private String buildPaymentUrl(String msisdn, BigDecimal amount, String mappedOrderId, String callbackLink) {
        String encodedCallback = Base64.getEncoder().encodeToString(callbackLink.getBytes());
        String encodedUpdate = Base64.getEncoder().encodeToString(updateStatusUrl.getBytes());

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
