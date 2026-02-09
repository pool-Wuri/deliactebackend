package com.deliacte.dto.response;

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
public class OrganisationResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private String address;
    private String telephone;
    private String email;
    private String website;
    private String logoUrl;
    private Boolean isActive;
    private UUID parentId;
    private String parentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer procedureCount;
    private Integer userCount;
    private ParameterSimpleResponse typeOrganisation;

}
