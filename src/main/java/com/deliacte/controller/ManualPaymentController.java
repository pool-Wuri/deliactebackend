package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ManualPaymentRequest;
import com.deliacte.dto.response.PaymentStatusResponse;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.ManualPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments/manual")
@RequiredArgsConstructor
public class ManualPaymentController {

    private final ManualPaymentService manualPaymentService;

    @PostMapping("/dossier/{dossierNumber}/validate")
    @Operation(summary = "Valider manuellement le paiement d'une procédure",
            description = "L'agent enregistre un paiement reçu pour l'ensemble du dossier/procédure.")
    public ApiResponse<PaymentStatusResponse> validateForDossier(
            @PathVariable String dossierNumber,
            @RequestBody ManualPaymentRequest request) {
        UUID agentId = SecurityUtils.getCurrentUserId();
        return manualPaymentService.validateForDossier(dossierNumber, request, agentId);
    }

    @PostMapping("/dossier/{dossierNumber}/operations/{dossierOperationId}/validate")
    @Operation(summary = "Valider manuellement le paiement d'une étape",
            description = "L'agent enregistre un paiement reçu pour une étape spécifique du dossier.")
    public ApiResponse<PaymentStatusResponse> validateForOperation(
            @PathVariable String dossierNumber,
            @PathVariable UUID dossierOperationId,
            @RequestBody ManualPaymentRequest request) {
        UUID agentId = SecurityUtils.getCurrentUserId();
        return manualPaymentService.validateForDossierOperation(dossierNumber, dossierOperationId, request, agentId);
    }

    @GetMapping("/dossier/{dossierNumber}/status")
    @Operation(summary = "Statut des paiements d'un dossier",
            description = "Retourne la liste des paiements enregistrés (procédure + opérations) et indique lesquels sont manquants.")
    public ApiResponse<List<PaymentStatusResponse>> getStatus(@PathVariable String dossierNumber) {
        return manualPaymentService.getStatusForDossier(dossierNumber);
    }
}
