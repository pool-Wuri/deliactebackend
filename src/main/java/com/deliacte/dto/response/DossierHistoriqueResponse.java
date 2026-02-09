package com.deliacte.dto.response;

import com.deliacte.enums.DossierStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DossierHistoriqueResponse {

    private UUID id;
    private DossierStatus status;
    private String comment;
    private LocalDateTime actionDate;
    private String userName;
    private String userEmail;
}
