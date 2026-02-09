package com.deliacte.service;

import com.deliacte.dto.request.PaymentRequest;
import com.deliacte.dto.response.PaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface ArzekaPaymentService {

    String initiatePaymentOnDossier(String dossierId, PaymentRequest dto);

    String initiatePaymentOnOperation(UUID operationId, PaymentRequest dto);

    PaymentResponse checkPaymentStatus(String mappedOrderId);

    PaymentResponse updatePaymentFromCallback(String status,
                                              String transTimeStamp,
                                              String paymentInfo,
                                              String paymentMethod,
                                              String accountInfo,
                                              String transId,
                                              String paymentRequestId);
}
