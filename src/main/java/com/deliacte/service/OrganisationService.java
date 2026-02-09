package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.OrganisationRequest;
import com.deliacte.dto.response.OrganisationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface OrganisationService {

    // === CRUD Operations ===
    ApiResponse<OrganisationResponse> create(OrganisationRequest request);

    ApiResponse<OrganisationResponse> update(UUID id, OrganisationRequest request);

    ApiResponse<OrganisationResponse> getById(UUID id);

    ApiResponse<PageResponse<OrganisationResponse>> getAll(Pageable pageable);

    ApiResponse<Void> delete(UUID id);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OrganisationResponse>> findByUserId(UUID userId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OrganisationResponse>> findByUserEmail(String email);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<OrganisationResponse>> getAllWithoutPagination();

    // === Recherche textuelle ===
    ApiResponse<PageResponse<OrganisationResponse>> search(String query, Pageable pageable);

    // === Activation/Désactivation ===
    ApiResponse<OrganisationResponse> toggleActive(UUID id);

}
