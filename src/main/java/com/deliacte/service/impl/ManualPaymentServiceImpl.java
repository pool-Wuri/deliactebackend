package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ManualPaymentRequest;
import com.deliacte.dto.response.PaymentStatusResponse;
import com.deliacte.entity.*;
import com.deliacte.repository.*;
import com.deliacte.service.ManualPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualPaymentServiceImpl implements ManualPaymentService {

    private final PaymentRepository paymentRepository;
    private final DossierRepository dossierRepository;
    private final DossierOperationRepository dossierOperationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<PaymentStatusResponse> validateForDossier(
            String dossierNumber,
            ManualPaymentRequest request,
            UUID agentId) {

        Dossier dossier = dossierRepository.findByDossierNumber(dossierNumber)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable : " + dossierNumber));

        String agentName = resolveAgentName(agentId);

        // Upsert : si un paiement manuel DOSSIER existe déjà, on le met à jour
        Payment payment = paymentRepository.findByDossier(dossier)
                .orElseGet(() -> Payment.builder()
                        .dossier(dossier)
                        .merchantId("MANUAL")
                        .mappedOrderId(generateRef())
                        .build());

        payment.setAmount(request.getAmount());
        payment.setTransId(request.getReference());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setManualNote(request.getNote());
        payment.setStatus("SUCCESS");
        payment.setPaymentInfo("COMPLETED");
        payment.setThirdPartyModule("MANUAL");
        payment.setValidatedByName(agentName);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        log.info("Paiement manuel validé pour le dossier {} par {}", dossierNumber, agentName);

        return ApiResponse.success(toStatusResponse(payment, dossier, null), "Paiement validé avec succès");
    }

    @Override
    @Transactional
    public ApiResponse<PaymentStatusResponse> validateForDossierOperation(
            String dossierNumber,
            UUID dossierOperationId,
            ManualPaymentRequest request,
            UUID agentId) {

        Dossier dossier = dossierRepository.findByDossierNumber(dossierNumber)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable : " + dossierNumber));

        DossierOperation dossierOp = dossierOperationRepository.findById(dossierOperationId)
                .orElseThrow(() -> new IllegalArgumentException("Étape introuvable : " + dossierOperationId));

        if (!dossierOp.getDossier().getId().equals(dossier.getId())) {
            throw new IllegalArgumentException("Cette étape n'appartient pas au dossier indiqué");
        }

        String agentName = resolveAgentName(agentId);

        Payment payment = paymentRepository.findByDossierOperation(dossierOp)
                .orElseGet(() -> Payment.builder()
                        .dossierOperation(dossierOp)
                        .merchantId("MANUAL")
                        .mappedOrderId(generateRef())
                        .build());

        payment.setAmount(request.getAmount());
        payment.setTransId(request.getReference());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setManualNote(request.getNote());
        payment.setStatus("SUCCESS");
        payment.setPaymentInfo("COMPLETED");
        payment.setThirdPartyModule("MANUAL");
        payment.setValidatedByName(agentName);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        log.info("Paiement manuel validé pour l'étape {} du dossier {} par {}", dossierOperationId, dossierNumber, agentName);

        return ApiResponse.success(toStatusResponse(payment, dossier, dossierOp), "Paiement validé avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PaymentStatusResponse>> getStatusForDossier(String dossierNumber) {
        Dossier dossier = dossierRepository.findByDossierNumber(dossierNumber)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable : " + dossierNumber));

        List<PaymentStatusResponse> results = new ArrayList<>();

        // Paiement procédure
        boolean procedurePaymentRequired = Boolean.TRUE.equals(dossier.getProcedure().getHasPayment());
        if (procedurePaymentRequired) {
            Optional<Payment> procPayment = paymentRepository.findByDossier(dossier);
            if (procPayment.isPresent()) {
                results.add(toStatusResponse(procPayment.get(), dossier, null));
            } else {
                results.add(PaymentStatusResponse.builder()
                        .scope("DOSSIER")
                        .dossierNumber(dossierNumber)
                        .paymentRequired(true)
                        .isPaid(false)
                        .requiredAmount(dossier.getProcedure().getFee())
                        .build());
            }
        }

        // Paiements opérations
        List<Payment> opPayments = paymentRepository.findAllByDossierOperation_Dossier(dossier);
        for (Payment p : opPayments) {
            results.add(toStatusResponse(p, dossier, p.getDossierOperation()));
        }

        // Opérations ayant hasPayment=true mais pas encore de paiement enregistré
        dossier.getOperationSteps().stream()
                .filter(step -> Boolean.TRUE.equals(step.getOperation().getHasPayment()))
                .filter(step -> opPayments.stream()
                        .noneMatch(p -> p.getDossierOperation() != null
                                && p.getDossierOperation().getId().equals(step.getId())))
                .forEach(step -> results.add(PaymentStatusResponse.builder()
                        .scope("OPERATION")
                        .dossierNumber(dossierNumber)
                        .dossierOperationId(step.getId())
                        .operationName(step.getOperation().getName())
                        .paymentRequired(true)
                        .isPaid(false)
                        .requiredAmount(step.getOperation().getFee())
                        .build()));

        return ApiResponse.success(results, "Statuts de paiement récupérés");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private PaymentStatusResponse toStatusResponse(Payment p, Dossier dossier, DossierOperation dossierOp) {
        boolean isPaid = "SUCCESS".equalsIgnoreCase(p.getStatus())
                && "COMPLETED".equalsIgnoreCase(p.getPaymentInfo());

        PaymentStatusResponse.PaymentStatusResponseBuilder b = PaymentStatusResponse.builder()
                .paymentId(p.getId())
                .dossierNumber(dossier.getDossierNumber())
                .paymentRequired(true)
                .isPaid(isPaid)
                .paidAmount(p.getAmount())
                .reference(p.getTransId())
                .paymentMethod(p.getPaymentMethod())
                .note(p.getManualNote())
                .validatedByName(p.getValidatedByName())
                .paidAt(p.getCreatedAt());

        if (dossierOp != null) {
            b.scope("OPERATION")
             .dossierOperationId(dossierOp.getId())
             .operationName(dossierOp.getOperation().getName())
             .requiredAmount(dossierOp.getOperation().getFee());
        } else {
            b.scope("DOSSIER")
             .requiredAmount(dossier.getProcedure().getFee());
        }

        return b.build();
    }

    private String resolveAgentName(UUID agentId) {
        if (agentId == null) return "Agent inconnu";
        return userRepository.findById(agentId)
                .map(User::getFullName)
                .orElse("Agent inconnu");
    }

    private String generateRef() {
        return "MANUAL-" + Instant.now().getEpochSecond() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
