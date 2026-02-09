package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.EntityObjectRequest;
import com.deliacte.dto.response.EntityObjectResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface EntityObjectService {
    ApiResponse<EntityObjectResponse> create(EntityObjectRequest request);
    ApiResponse<EntityObjectResponse> update(UUID id, EntityObjectRequest request);
    ApiResponse<EntityObjectResponse> getById(UUID id);
    ApiResponse<PageResponse<EntityObjectResponse>> getAll(Pageable pageable);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<EntityObjectResponse>>
    getEntityObjectsByOperationId(UUID operationId);

    ApiResponse<Void> delete(UUID id);
    ApiResponse<EntityObjectResponse> activate(UUID id);
    ApiResponse<EntityObjectResponse> deactivate(UUID id);
}
