package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ProcedureRequest;
import com.deliacte.dto.response.ProcedureResponse;
import com.deliacte.enums.ProcedureStatus;
import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.ProcedureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/procedures")
@RequiredArgsConstructor
@Tag(name = "Procédures", description = "API de gestion des procédures administratives")
public class ProcedureController {

    private final ProcedureService procedureService;

    @PostMapping
    @Operation(summary = "Créer une procédure")
    public ResponseEntity<ApiResponse<ProcedureResponse>> createProcedure(@Valid @RequestBody ProcedureRequest request) {
        ApiResponse<ProcedureResponse> response = procedureService.create(request);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une procédure")
    public ResponseEntity<ApiResponse<ProcedureResponse>> updateProcedure(
            @PathVariable UUID id,
            @Valid @RequestBody ProcedureRequest request) {
        ApiResponse<ProcedureResponse> response = procedureService.update(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une procédure par ID")
    public ResponseEntity<ApiResponse<ProcedureResponse>> getProcedureById(@PathVariable UUID id) {
        return ResponseEntity.ok(procedureService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les procédures")
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getAllProcedures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(procedureService.getAll(pageable));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Lister les procédures de l'utilisateur connecté",
            description = "Récupère toutes les procédures associées à l'utilisateur authentifié"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getMyProcedures() {

        UUID userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();
        UserRole role = SecurityUtils.getCurrentUserRole();

        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Utilisateur non authentifié"));
        }

        if (role == UserRole.RESPONSABLE_ORGANISATION) {
            return ResponseEntity.ok(
                    procedureService.findByResponsableOrganisationUserEmail(email)
            );
        }

        if (role == UserRole.ADMIN_PROCEDURE) {
            return ResponseEntity.ok(
                    procedureService.findByResponsableProcedureUserEmail(email)
            );
        }

        if (role == UserRole.AGENT) {
            return ResponseEntity.ok(
                    procedureService.findProceduresByUserOperations(userId)
            );
        }

        // Cas par défaut : rôle non autorisé
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Accès non autorisé pour ce rôle"));
    }


    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Lister les procédures d'un utilisateur",
            description = "Récupère toutes les procédures associées à un utilisateur donné"
    )
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getProceduresByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(procedureService.findByUserId(userId));
    }



    @GetMapping("/public")
    @Operation(summary = "Lister les procédures publiques (public)")
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getPublicProcedures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(procedureService.getPublicProcedures(pageable));
    }


    @GetMapping("/top6")
    @Operation(summary = "Lister le Top 6 des procédures par nombre de dossiers")
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getTop6Procedures(
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 6);
        return ResponseEntity.ok(procedureService.getAllTop6(pageable));
    }



    @GetMapping("/organisation/{organisationId}")
    @Operation(summary = "Lister les procédures d'une organisation")
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getProceduresByOrganisation(
            @PathVariable UUID organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(procedureService.getByOrganisation(organisationId, pageable));
    }

    @GetMapping("/allorganisation/{organisationId}")
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> getProceduresByOrganisation(
            @PathVariable UUID organisationId) {

        return ResponseEntity.ok(
                procedureService.getByOrganisationNoPage(organisationId)
        );
    }



    @GetMapping("/user-status/{userStatus}")
    @Operation(summary = "Lister les procédures accessibles par statut utilisateur")
    public ResponseEntity<ApiResponse<List<ProcedureResponse>>> getProceduresByUserStatus(
            @PathVariable UserStatus userStatus) {
        return ResponseEntity.ok(procedureService.getByUserStatus(userStatus));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des procédures")
    public ResponseEntity<ApiResponse<PageResponse<ProcedureResponse>>> searchProcedures(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(procedureService.search(q, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une procédure")
    public ResponseEntity<ApiResponse<Void>> deleteProcedure(@PathVariable UUID id) {
        ApiResponse<Void> response = procedureService.delete(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Changer le statut d'une procédure")
    public ResponseEntity<ApiResponse<ProcedureResponse>> changeStatus(
            @PathVariable UUID id,
            @RequestParam ProcedureStatus status) {
        ApiResponse<ProcedureResponse> response = procedureService.changeStatus(id, status);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer une procédure")
    public ResponseEntity<ApiResponse<ProcedureResponse>> activateProcedure(@PathVariable UUID id) {
        return ResponseEntity.ok(procedureService.changeStatus(id, ProcedureStatus.PUBLISHED));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspendre une procédure")
    public ResponseEntity<ApiResponse<ProcedureResponse>> suspendProcedure(@PathVariable UUID id) {
        return ResponseEntity.ok(procedureService.changeStatus(id, ProcedureStatus.SUSPENDED));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver une procédure")
    public ResponseEntity<ApiResponse<ProcedureResponse>> archiveProcedure(@PathVariable UUID id) {
        return ResponseEntity.ok(procedureService.changeStatus(id, ProcedureStatus.ARCHIVED));
    }
}
