package com.deliacte.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenPaymentInitiateRequest {

    /** Numéro mobile au format international : 22670XXXXXX */
    private String msisdn;
}
