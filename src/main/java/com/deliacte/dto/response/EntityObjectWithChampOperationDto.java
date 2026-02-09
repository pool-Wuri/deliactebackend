package com.deliacte.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityObjectWithChampOperationDto {

    private UUID entityId;
    private String entityCode;
    private String entityLabel;

    private List<ChampOperationResponseDto> champOperations;
}
