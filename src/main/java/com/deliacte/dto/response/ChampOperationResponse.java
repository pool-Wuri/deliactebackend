package com.deliacte.dto.response;

import com.deliacte.enums.EnumInputFieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampOperationResponse {

    private UUID id;
    private String code;
    private String label;
    private String placeholder;
    private String helpText;
    private EnumInputFieldType fieldType;
    private Boolean required;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private String defaultValue;
    private Integer orderIndex;
    private Boolean active;
    private OperationSimpleResponse operation;
    private List<OptionChampOperationResponse> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
