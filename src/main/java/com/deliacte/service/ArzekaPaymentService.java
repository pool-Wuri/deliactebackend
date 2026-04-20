package com.deliacte.service;

import com.deliacte.dto.request.PaymentRequest;
import com.deliacte.dto.response.PaymentResponse;

import java.util.UUID;

public interface ArzekaPaymentService {

    String initiatePaymentOnDossier(String dossierId, PaymentRequest dto);

    String initiatePaymentOnOperation(UUID operationId, PaymentRequest dto);

    /** Initie un paiement Arzeka pour le citoyen (montant pris du fee de la procédure) */
    String initiateCitizenPaymentForDossier(String dossierNumber, String msisdn);

    /** Initie un paiement Arzeka pour le citoyen (montant pris du fee de l'opération) */
    String initiateCitizenPaymentForOperation(String dossierNumber, UUID dossierOperationId, String msisdn);

    PaymentResponse checkPaymentStatus(String mappedOrderId);

    PaymentResponse updatePaymentFromCallback(String status,
                                              String transTimeStamp,
                                              String paymentInfo,
                                              String paymentMethod,
                                              String accountInfo,
                                              String transId,
                                              String paymentRequestId);
}
