package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ProcedureRequest;
import com.deliacte.dto.response.ProcedureResponse;
import com.deliacte.enums.ProcedureStatus;
import com.deliacte.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ProcedureService {

    // === CRUD Operations ===
    ApiResponse<ProcedureResponse> create(ProcedureRequest request);

    ApiResponse<ProcedureResponse> update(UUID id, ProcedureRequest request);

    ApiResponse<ProcedureResponse> getById(UUID id);

    ApiResponse<PageResponse<ProcedureResponse>> getAll(Pageable pageable);

    ApiResponse<Void> delete(UUID id);

    // === Recherche par organisation ===
    ApiResponse<PageResponse<ProcedureResponse>> getByOrganisation(UUID organisationId, Pageable pageable);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> getAllTop6(Pageable pageable);

    // === Procédures publiques ===
    ApiResponse<PageResponse<ProcedureResponse>> getPublicProcedures(Pageable pageable);



    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> getByOrganisationNoPage(UUID organisationId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> findByUserId(UUID userId);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> findByUserEmail(String email);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> findByResponsableProcedureUserEmail(String email);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> findProceduresByUserOperations(UUID id);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<ProcedureResponse>> findByResponsableOrganisationUserEmail(String email);

    // === Recherche textuelle ===
    ApiResponse<PageResponse<ProcedureResponse>> search(String query, Pageable pageable);

    // === Recherche par statut utilisateur ===
    ApiResponse<List<ProcedureResponse>> getByUserStatus(UserStatus userStatus);

    // === Changement de statut ===
    ApiResponse<ProcedureResponse> changeStatus(UUID id, ProcedureStatus status);
}
