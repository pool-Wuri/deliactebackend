package com.deliacte.controller;

import com.deliacte.dto.request.CitizenPaymentInitiateRequest;
import com.deliacte.dto.request.PaymentRequest;
import com.deliacte.dto.response.PaymentResponse;
import com.deliacte.service.ArzekaPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/payments/arzeka")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ArzekaPaymentController {

    private final ArzekaPaymentService arzekaService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // Admin / opérateur
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/dossier/{dossierId}/initiate")
    @Operation(summary = "Initier un paiement Arzeka sur un dossier (admin)")
    public ResponseEntity<String> initiateOnDossier(@PathVariable String dossierId,
                                                    @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(arzekaService.initiatePaymentOnDossier(dossierId, request));
    }

    @PostMapping("/operation/{operationId}/initiate")
    @Operation(summary = "Initier un paiement Arzeka sur une opération (admin)")
    public ResponseEntity<String> initiateOnOperation(@PathVariable UUID operationId,
                                                      @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(arzekaService.initiatePaymentOnOperation(operationId, request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Citoyen : montant automatique depuis le fee
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/dossier/{dossierNumber}/citizen-initiate")
    @Operation(summary = "Initier un paiement Arzeka pour le citoyen (frais procédure)")
    public ResponseEntity<String> citizenInitiateForDossier(
            @PathVariable String dossierNumber,
            @RequestBody CitizenPaymentInitiateRequest request) {
        String paymentUrl = arzekaService.initiateCitizenPaymentForDossier(dossierNumber, request.getMsisdn());
        return ResponseEntity.ok(paymentUrl);
    }

    @PostMapping("/dossier/{dossierNumber}/operations/{dossierOperationId}/citizen-initiate")
    @Operation(summary = "Initier un paiement Arzeka pour le citoyen (frais opération)")
    public ResponseEntity<String> citizenInitiateForOperation(
            @PathVariable String dossierNumber,
            @PathVariable UUID dossierOperationId,
            @RequestBody CitizenPaymentInitiateRequest request) {
        String paymentUrl = arzekaService.initiateCitizenPaymentForOperation(
                dossierNumber, dossierOperationId, request.getMsisdn());
        return ResponseEntity.ok(paymentUrl);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Callback Arzeka : retour citoyen + notification serveur
    // Arzeka appelle ce endpoint (GET) avec tous les paramètres de résultat.
    // On met à jour le paiement et on redirige vers le frontend.
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/return")
    @Operation(summary = "Retour Arzeka après paiement (redirect citoyen + mise à jour)")
    public ResponseEntity<Void> handleReturn(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transTimeStamp,
            @RequestParam(required = false) String paymentInfo,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String accountInfo,
            @RequestParam(required = false) String transId,
            @RequestParam(required = false) String paymentRequestID,
            @RequestParam(required = false) String mappedOrderId,
            @RequestParam(required = false) String dossierNumber,
            @RequestParam(required = false) String dossierOperationId) {

        // Arzeka peut utiliser "paymentRequestID" ou "paymentRequestId"
        String orderId = paymentRequestID != null ? paymentRequestID : mappedOrderId;

        if (orderId != null && status != null) {
            try {
                arzekaService.updatePaymentFromCallback(
                        status,
                        transTimeStamp != null ? transTimeStamp : "",
                        paymentInfo != null ? paymentInfo : "",
                        paymentMethod != null ? paymentMethod : "",
                        accountInfo != null ? accountInfo : "",
                        transId != null ? transId : "",
                        orderId);
                log.info("Retour Arzeka traité — orderId={} status={} paymentInfo={}", orderId, status, paymentInfo);
            } catch (Exception e) {
                log.warn("Erreur lors du traitement du retour Arzeka : {}", e.getMessage());
            }
        }

        // Construire l'URL de redirection frontend
        String redirectBase = frontendUrl + "/paiement-retour";
        StringBuilder redirect = new StringBuilder(redirectBase + "?status=" + (status != null ? status : "UNKNOWN"));
        if (dossierNumber != null)      redirect.append("&dossierNumber=").append(dossierNumber);
        if (dossierOperationId != null) redirect.append("&dossierOperationId=").append(dossierOperationId);
        if (orderId != null)            redirect.append("&ref=").append(orderId);
        if (paymentInfo != null)        redirect.append("&paymentInfo=").append(paymentInfo);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect.toString()))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Check statut
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/check/{mappedOrderId}")
    @Operation(summary = "Vérifier le statut d'un paiement auprès d'Arzeka")
    public ResponseEntity<PaymentResponse> checkPayment(@PathVariable String mappedOrderId) {
        return ResponseEntity.ok(arzekaService.checkPaymentStatus(mappedOrderId));
    }
}
