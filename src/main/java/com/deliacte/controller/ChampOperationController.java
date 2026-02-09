package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ChampOperationRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.response.ChampOperationResponse;
import com.deliacte.dto.response.OperationFormResponseDto;
import com.deliacte.service.ChampOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/champs")
@RequiredArgsConstructor
@Tag(name = "Champs d'opération", description = "Gestion des champs de formulaire des opérations")
public class ChampOperationController {
    
    private final ChampOperationService champOperationService;

    @PostMapping
    @Operation(summary = "Créer un champ", description = "Crée un nouveau champ pour une opération")
    public ResponseEntity<ApiResponse<ChampOperationResponse>> create(@Valid @RequestBody ChampOperationRequest request) {
        return ResponseEntity.ok(champOperationService.create(request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un champ", description = "Met à jour un champ existant")
    public ResponseEntity<ApiResponse<ChampOperationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ChampOperationRequest request) {
        return ResponseEntity.ok(champOperationService.update(id, request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un champ", description = "Récupère un champ par son ID")
    public ResponseEntity<ApiResponse<ChampOperationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(champOperationService.getById(id));
    }
    
    @GetMapping
    @Operation(summary = "Lister les champs", description = "Récupère la liste paginée des champs")
    public ResponseEntity<ApiResponse<PageResponse<ChampOperationResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderIndex") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(champOperationService.getAll(pageable));
    }
    
    @GetMapping("/operation/{operationId}")
    @Operation(summary = "Lister les champs d'une opération", description = "Récupère tous les champs d'une opération")
    public ResponseEntity<ApiResponse<List<ChampOperationResponse>>> getByOperationId(@PathVariable UUID operationId) {
        return ResponseEntity.ok(champOperationService.getByOperationId(operationId));
    }


    @GetMapping("/entityobject/{entityId}")
    @Operation(summary = "Lister les champs d'une entité objet", description = "Récupère tous les champs d'une entité objet")
    public ResponseEntity<ApiResponse<List<ChampOperationResponse>>> getByEntityObjectId(@PathVariable UUID entityId) {
        return ResponseEntity.ok(champOperationService.getByEntityObjectId(entityId));
    }


    @GetMapping("/operation/{operationId}/public")
    @Operation(summary = "Lister les champs publics d'une opération", description = "Récupère les champs actifs d'une opération pour les formulaires publics")
    public ResponseEntity<ApiResponse<List<ChampOperationResponse>>> getPublicByOperationId(@PathVariable UUID operationId) {
        return ResponseEntity.ok(champOperationService.getPublicByOperationId(operationId));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un champ", description = "Supprime un champ (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(champOperationService.delete(id));
    }
    
    @PostMapping("/operation/{operationId}/reorder")
    @Operation(summary = "Réordonner les champs", description = "Réordonne les champs d'une opération")
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable UUID operationId,
            @RequestBody List<UUID> champIds) {
        return ResponseEntity.ok(champOperationService.reorder(operationId, champIds));
    }


    @PostMapping("/{operationId}/entity/{entityId}/link-champs")
    @Operation(
            summary = "Lier une entité et ses champs à une opération",
            description = "Associe un EntityObject à une opération et lie une liste ordonnée de champs (ChampOperation)"
    )
    public ResponseEntity<ApiResponse<Void>> linkEntityAndChamps(
            @PathVariable UUID operationId,
            @PathVariable UUID entityId,
            @RequestBody IdListRequest champIds
    ) {
        return ResponseEntity.ok(
                champOperationService.linkEntityAndChampOperationToOperation(
                        entityId,
                        operationId,
                        champIds
                )
        );
    }
    @PostMapping("/{operationId}/operation/link-champs")
    @Operation(
            summary = "Lier une entité et ses champs à une opération",
            description = "Associe un EntityObject à une opération et lie une liste ordonnée de champs (ChampOperation)"
    )
    public ResponseEntity<ApiResponse<List<ChampOperationResponse>>> linkEntityAndChamps(
            @PathVariable UUID operationId,
            @RequestBody IdListRequest champIds
    ) {
        // Appel au service pour associer les champs
        ApiResponse<List<ChampOperationResponse>> response =
                champOperationService.associateChampIdsToOperation(operationId, champIds);

        return ResponseEntity.ok(response);
    }



    @PostMapping("/entity/{entityId}/reorder")
    @Operation(summary = "Réordonner les champs d'une entité", description = "Réordonne les champs d'une entity")
    public ResponseEntity<ApiResponse<Void>> reorderEntity(
            @PathVariable UUID entityId,
            @RequestBody List<UUID> champIds) {
        return ResponseEntity.ok(champOperationService.reorderEntity(entityId, champIds));
    }

    @PostMapping("/multiples")
    @Operation(summary = "Créer une liste de champs avec leurs options")
    public ApiResponse<List<ChampOperationResponse>> createAll(
            @RequestBody @Valid List<ChampOperationRequest> requests) {

        return champOperationService.createAll(requests);
    }

    @GetMapping("/operation-form")
    @Operation(summary = "Récupérer le formulaire complet d'une opération")
    public ApiResponse<OperationFormResponseDto> getOperationForm(
            @RequestParam(value = "operationId", required = false) UUID operationId,
            @RequestParam(value = "procedureId", required = false) UUID procedureId) {

        return champOperationService.getOperationForm(operationId, procedureId);
    }



}
