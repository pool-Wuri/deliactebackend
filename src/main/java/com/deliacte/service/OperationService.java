package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OperationRequest;
import com.deliacte.dto.response.OperationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface OperationService {
    
    ApiResponse<OperationResponse> create(OperationRequest request);
    
    ApiResponse<OperationResponse> update(UUID id, OperationRequest request);
    
    ApiResponse<OperationResponse> getById(UUID id);
    
    ApiResponse<PageResponse<OperationResponse>> getAll(Pageable pageable);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OperationResponse>>  getOperationsFromUserOrganisations(UUID userId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OperationResponse>>  getOperationsFromUserProcedures(UUID userId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OperationResponse>>  getOperationsFromUserAgent(UUID userId);

    ApiResponse<List<OperationResponse>> getByProcedureId(UUID procedureId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OperationResponse>>
    getOperationsByOrganisationId(UUID organisationId);

    ApiResponse<Void> delete(UUID id);
    
    ApiResponse<OperationResponse> activate(UUID id);
    
    ApiResponse<OperationResponse> deactivate(UUID id);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OperationResponse>> getNextOperations(UUID operationId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OperationResponse>> getPreviousOperations(UUID operationId);

    // =====================================================
    // ➕ Ajouter des opérations suivantes
    // =====================================================
    @Transactional
    ApiResponse<Void> addNextOperations(UUID operationId, IdListRequest nextOperationIds);

    // =====================================================
    // ➕ Ajouter des opérations précédentes
    // =====================================================
    @Transactional
    ApiResponse<Void> addPreviousOperations(UUID operationId, IdListRequest previousOperationIds);

    // =====================================================
    // ➖ Retirer des opérations suivantes
    // =====================================================
    @Transactional
    ApiResponse<Void> removeNextOperations(UUID operationId, IdListRequest nextOperationIds);

    // =====================================================
    // ➖ Retirer des opérations précédentes
    // =====================================================
    @Transactional
    ApiResponse<Void> removePreviousOperations(UUID operationId, IdListRequest previousOperationIds);

    ApiResponse<Void> reorder(UUID procedureId, List<UUID> operationIds);

    @Transactional
    ApiResponse<Void> removeEntityObjectFromOperation(
            UUID operationId,
            UUID entityObjectId
    );

    @Transactional
    ApiResponse<String> uploadTemplate(
            UUID operationId,
            MultipartFile templateFile
    );

    ApiResponse<String> generatePdfFromWordTemplate(UUID operationId, String numeroDossier);
}
