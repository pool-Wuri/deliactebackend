package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ChampOperationRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.response.ChampOperationResponse;
import com.deliacte.dto.response.OperationFormResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ChampOperationService {
    
    ApiResponse<ChampOperationResponse> create(ChampOperationRequest request);
    
    ApiResponse<ChampOperationResponse> update(UUID id, ChampOperationRequest request);

    @Transactional
    ApiResponse<List<ChampOperationResponse>> associateChampIdsToOperation(UUID entityId,
             IdListRequest champIds);

    ApiResponse<ChampOperationResponse> getById(UUID id);
    
    ApiResponse<PageResponse<ChampOperationResponse>> getAll(Pageable pageable);
    
    ApiResponse<List<ChampOperationResponse>> getByOperationId(UUID operationId);

    ApiResponse<List<ChampOperationResponse>> getByEntityObjectId(UUID entityObjectId);

    ApiResponse<List<ChampOperationResponse>> getPublicByOperationId(UUID operationId);
    
    ApiResponse<Void> delete(UUID id);
    
    ApiResponse<Void> reorder(UUID operationId, List<UUID> champIds);

    @Transactional
    ApiResponse<Void> linkEntityAndChampOperationToOperation(
            UUID entityId,
            UUID operationId,
            IdListRequest champIds
    );

    ApiResponse<Void> reorderEntity(UUID entityId, List<UUID> champIds);

    ApiResponse <OperationFormResponseDto> getOperationForm(UUID operationId,UUID procedureId);

    ApiResponse<List<ChampOperationResponse>> createAll(
            List<ChampOperationRequest> requests);
}
