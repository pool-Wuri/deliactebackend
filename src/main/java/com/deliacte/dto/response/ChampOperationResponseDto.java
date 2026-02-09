package com.deliacte.dto.response;

import com.deliacte.enums.EnumInputFieldType;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampOperationResponseDto {

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
    private Integer order;
    private Integer orderIndex;
    private Boolean active;

    private List<OptionChampOperationResponse> options;
}
