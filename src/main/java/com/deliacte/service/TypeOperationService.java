package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.TypeOperationRequest;
import com.deliacte.dto.response.TypeOperationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TypeOperationService {
    
    ApiResponse<TypeOperationResponse> create(TypeOperationRequest request);
    
    ApiResponse<TypeOperationResponse> update(UUID id, TypeOperationRequest request);
    
    ApiResponse<TypeOperationResponse> getById(UUID id);
    
    ApiResponse<PageResponse<TypeOperationResponse>> getAll(Pageable pageable);
    
    ApiResponse<List<TypeOperationResponse>> getAllActive();
    
    ApiResponse<Void> delete(UUID id);
    
    ApiResponse<TypeOperationResponse> activate(UUID id);
    
    ApiResponse<TypeOperationResponse> deactivate(UUID id);
}
