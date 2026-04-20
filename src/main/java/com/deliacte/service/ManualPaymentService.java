package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ManualPaymentRequest;
import com.deliacte.dto.response.PaymentStatusResponse;

import java.util.List;
import java.util.UUID;

public interface ManualPaymentService {

    /** Valide manuellement le paiement pour l'ensemble de la procédure du dossier */
    ApiResponse<PaymentStatusResponse> validateForDossier(
            String dossierNumber,
            ManualPaymentRequest request,
            UUID agentId
    );

    /** Valide manuellement le paiement pour une étape (DossierOperation) spécifique */
    ApiResponse<PaymentStatusResponse> validateForDossierOperation(
            String dossierNumber,
            UUID dossierOperationId,
            ManualPaymentRequest request,
            UUID agentId
    );

    /** Retourne le statut de tous les paiements (procédure + opérations) pour un dossier */
    ApiResponse<List<PaymentStatusResponse>> getStatusForDossier(String dossierNumber);
}
