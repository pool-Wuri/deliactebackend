package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OperationRequest;
import com.deliacte.dto.response.OperationResponse;
import com.deliacte.enums.UserRole;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/operations")
@RequiredArgsConstructor
@Tag(name = "Opérations", description = "Gestion des opérations des procédures")
public class OperationController {
    
    private final OperationService operationService;
    
    @PostMapping
    @Operation(summary = "Créer une opération", description = "Crée une nouvelle opération pour une procédure")
    public ResponseEntity<ApiResponse<OperationResponse>> create(@Valid @RequestBody OperationRequest request) {
        return ResponseEntity.ok(operationService.create(request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une opération", description = "Met à jour une opération existante")
    public ResponseEntity<ApiResponse<OperationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OperationRequest request) {
        return ResponseEntity.ok(operationService.update(id, request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une opération", description = "Récupère une opération par son ID")
    public ResponseEntity<ApiResponse<OperationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(operationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lister les opérations", description = "Récupère la liste paginée des opérations")
    public ResponseEntity<ApiResponse<PageResponse<OperationResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderIndex") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        ApiResponse<PageResponse<OperationResponse>> response = operationService.getAll(pageable);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/procedure/{procedureId}")
    @Operation(summary = "Lister les opérations d'une procédure", description = "Récupère toutes les opérations d'une procédure")
    public ResponseEntity<ApiResponse<List<OperationResponse>>> getByProcedureId(@PathVariable UUID procedureId) {
        return ResponseEntity.ok(operationService.getByProcedureId(procedureId));
    }

    @GetMapping("/organisation/{organisationId}")
    @Operation(summary = "Lister les opérations d'une organisation", description = "Récupère toutes les opérations d'une organisation")
    public ResponseEntity<ApiResponse<PageResponse<OperationResponse>>> getOperationsByOrganisationId(@PathVariable UUID organisationId) {
        return ResponseEntity.ok(operationService.getOperationsByOrganisationId(organisationId));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une opération", description = "Supprime une opération (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(operationService.delete(id));
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer une opération", description = "Active une opération")
    public ResponseEntity<ApiResponse<OperationResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(operationService.activate(id));
    }
    
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver une opération", description = "Désactive une opération")
    public ResponseEntity<ApiResponse<OperationResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(operationService.deactivate(id));
    }
    
    @PostMapping("/procedure/{procedureId}/reorder")
    @Operation(summary = "Réordonner les opérations", description = "Réordonne les opérations d'une procédure")
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable UUID procedureId,
            @RequestBody List<UUID> operationIds) {
        return ResponseEntity.ok(operationService.reorder(procedureId, operationIds));
    }

    // ======================================================
    // 🔜 Opérations suivantes
    // ======================================================
    @GetMapping("/{operationId}/next")
    @Operation(
            summary = "Lister les opérations suivantes",
            description = "Récupère les opérations suivantes d'une opération donnée"
    )
    public ResponseEntity<ApiResponse<PageResponse<OperationResponse>>> getNextOperations(
            @PathVariable UUID operationId
    ) {
        return ResponseEntity.ok(
                operationService.getNextOperations(operationId)
        );
    }

    // ======================================================
    // 🔙 Opérations précédentes
    // ======================================================
    @GetMapping("/{operationId}/previous")
    @Operation(
            summary = "Lister les opérations précédentes",
            description = "Récupère les opérations précédentes d'une opération donnée"
    )
    public ResponseEntity<ApiResponse<PageResponse<OperationResponse>>> getPreviousOperations(
            @PathVariable UUID operationId
    ) {
        return ResponseEntity.ok(
                operationService.getPreviousOperations(operationId)
        );
    }

    // =====================================================
    // ➕ Ajouter des opérations suivantes
    // =====================================================
    @PostMapping("/{operationId}/next")
    @Operation(
            summary = "Ajouter des opérations suivantes",
            description = "Associe une liste d'opérations comme suivantes à une opération"
    )
    public ResponseEntity<ApiResponse<Void>> addNextOperations(
            @PathVariable UUID operationId,
            @RequestBody IdListRequest nextOperationIds
    ) {
        return ResponseEntity.ok(
                operationService.addNextOperations(operationId, nextOperationIds)
        );
    }

    // =====================================================
    // ➕ Ajouter des opérations précédentes
    // =====================================================
    @PostMapping("/{operationId}/previous")
    @Operation(
            summary = "Ajouter des opérations précédentes",
            description = "Associe une liste d'opérations comme précédentes à une opération"
    )
    public ResponseEntity<ApiResponse<Void>> addPreviousOperations(
            @PathVariable UUID operationId,
            @RequestBody IdListRequest previousOperationIds
    ) {
        return ResponseEntity.ok(
                operationService.addPreviousOperations(operationId, previousOperationIds)
        );
    }

    // =====================================================
    // ➖ Retirer des opérations suivantes
    // =====================================================
    @PostMapping("/next/{operationId}")
    @Operation(
            summary = "Retirer des opérations suivantes",
            description = "Supprime le lien entre une opération et ses opérations suivantes"
    )
    public ResponseEntity<ApiResponse<Void>> removeNextOperations(
            @PathVariable UUID operationId,
            @RequestBody IdListRequest nextOperationIds
    ) {
        return ResponseEntity.ok(
                operationService.removeNextOperations(operationId, nextOperationIds)
        );
    }

    // =====================================================
    // ➖ Retirer des opérations précédentes
    // =====================================================
    @PostMapping("/previous/{operationId}")
    @Operation(
            summary = "Retirer des opérations précédentes",
            description = "Supprime le lien entre une opération et ses opérations précédentes"
    )
    public ResponseEntity<ApiResponse<Void>> removePreviousOperations(
            @PathVariable UUID operationId,
            @RequestBody IdListRequest previousOperationIds
    ) {
        return ResponseEntity.ok(
                operationService.removePreviousOperations(operationId, previousOperationIds)
        );
    }

    @DeleteMapping("/{operationId}/entities/{entityObjectId}")
    @Operation(
            summary = "Retirer une entité d'une opération",
            description = "Supprime l'association entre un EntityObject et une Operation"
    )
    public ResponseEntity<ApiResponse<Void>> removeEntityObjectFromOperation(
            @PathVariable UUID operationId,
            @PathVariable UUID entityObjectId
    ) {
        ApiResponse<Void> response =
                operationService.removeEntityObjectFromOperation(operationId, entityObjectId);

        return ResponseEntity.ok(response);
    }




    @GetMapping("/me")
    @Operation(
            summary = "Lister les opérations de l'utilisateur connecté",
            description = "Récupère toutes les opérations accessibles par l'utilisateur authentifié"
    )
    public ResponseEntity<ApiResponse<PageResponse<OperationResponse>>> getMyOperations() {

        UUID userId = SecurityUtils.getCurrentUserId();
        UserRole role = SecurityUtils.getCurrentUserRole();

        if (userId == null || role == null ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Utilisateur non authentifié"));
        }

        if (role == UserRole.RESPONSABLE_ORGANISATION) {
            return ResponseEntity.ok(
                    operationService.getOperationsFromUserOrganisations(userId)
            );
        }

        if (role == UserRole.ADMIN_PROCEDURE) {
            return ResponseEntity.ok(
                    operationService.getOperationsFromUserProcedures(userId)
            );
        }
        if (role == UserRole.AGENT) {
            return ResponseEntity.ok(
                    operationService.getOperationsFromUserAgent(userId)
            );
        }



        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Accès non autorisé pour ce rôle"));
    }




    @PostMapping("/{operationId}/template")
    @Operation(
            summary = "Uploader un template pour une opération",
            description = "Associe un fichier template (PDF, DOCX…) à une opération"
    )
    public ResponseEntity<ApiResponse<String>> uploadTemplate(
            @PathVariable UUID operationId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                operationService.uploadTemplate(operationId, file)
        );
    }




}
