package com.deliacte.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityObjectRequest {
    private String name;
    private String code;
    private String description;
    private Boolean isActive;
}
