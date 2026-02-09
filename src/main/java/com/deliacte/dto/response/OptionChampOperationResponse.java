package com.deliacte.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionChampOperationResponse {

    private UUID id;
    private String label;
    private String value;
    private Integer ordre;
    private Integer orderIndex;
    private Boolean isDefault;
    UUID champOperationId;
}
