package com.deliacte.controller;

import com.deliacte.dto.request.PaymentRequest;
import com.deliacte.dto.response.PaymentResponse;
import com.deliacte.service.ArzekaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ArzekaPaymentController {

    private final ArzekaPaymentService arzekaService;

    /**
     * Initier un paiement sur un dossier
     */
    @PostMapping("/dossier/{dossierId}/initiate")
    public ResponseEntity<String> initiateOnDossier(@PathVariable String dossierId,
                                                    @RequestBody PaymentRequest request) {
        String paymentUrl = arzekaService.initiatePaymentOnDossier(dossierId, request);
        return ResponseEntity.ok(paymentUrl);
    }

    /**
     * Initier un paiement sur une opération
     */
    @PostMapping("/operation/{operationId}/initiate")
    public ResponseEntity<String> initiateOnOperation(@PathVariable UUID operationId,
                                                      @RequestBody PaymentRequest request) {
        String paymentUrl = arzekaService.initiatePaymentOnOperation(operationId, request);
        return ResponseEntity.ok(paymentUrl);
    }

    /**
     * Callback reçu depuis ARZEKA après tentative de paiement
     */
    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam String status,
            @RequestParam String transTimeStamp,
            @RequestParam String paymentInfo,
            @RequestParam String paymentMethod,
            @RequestParam String accountInfo,
            @RequestParam String transId,
            @RequestParam String paymentRequestId) {

        PaymentResponse response = arzekaService.updatePaymentFromCallback(
                status, transTimeStamp, paymentInfo, paymentMethod,
                accountInfo, transId, paymentRequestId
        );

        // Notification côté API : si tout est OK, on marque payé
        if ("SUCCESS".equalsIgnoreCase(response.getStatus())
                && "COMPLETED".equalsIgnoreCase(response.getPaymentInfo())) {
            return ResponseEntity.ok("Paiement confirmé et enregistré.");
        }

        return ResponseEntity.ok("Callback reçu, statut: " + response.getStatus());
    }

    /**
     * Vérifier le statut d’un paiement auprès d’ARZEKA
     */
    @GetMapping("/check/{mappedOrderId}")
    public ResponseEntity<PaymentResponse> checkPayment(@PathVariable String mappedOrderId) {
        PaymentResponse response = arzekaService.checkPaymentStatus(mappedOrderId);
        return ResponseEntity.ok(response);
    }
}
