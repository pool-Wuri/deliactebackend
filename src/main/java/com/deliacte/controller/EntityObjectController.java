package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.EntityObjectRequest;
import com.deliacte.dto.response.EntityObjectResponse;
import com.deliacte.service.EntityObjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/entityobject")
@RequiredArgsConstructor
public class EntityObjectController {

    private final EntityObjectService entityObjectService;

    @PostMapping
    @Operation(
            summary = "Créer un EntityObject",
            description = "Crée une nouvelle entité métier (EntityObject) pouvant être associée à des opérations et des champs"
    )
    public ApiResponse<EntityObjectResponse> create(@RequestBody EntityObjectRequest request) {
        return entityObjectService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier un EntityObject",
            description = "Met à jour les informations d'une entité métier existante identifiée par son ID"
    )
    public ApiResponse<EntityObjectResponse> update(
            @PathVariable UUID id,
            @RequestBody EntityObjectRequest request
    ) {
        return entityObjectService.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consulter un EntityObject par ID",
            description = "Récupère le détail d'une entité métier (EntityObject) à partir de son identifiant unique"
    )
    public ApiResponse<EntityObjectResponse> getById(@PathVariable UUID id) {
        return entityObjectService.getById(id);
    }

    @GetMapping
    @Operation(
            summary = "Lister les EntityObjects",
            description = "Récupère la liste paginée de toutes les entités métier (EntityObjects)"
    )
    public ApiResponse<PageResponse<EntityObjectResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return entityObjectService.getAll(pageable);
    }

    @GetMapping("/operation/{operationId}")
    @Operation(
            summary = "Lister les EntityObjects d'une opération",
            description = "Récupère la liste des entités métier (EntityObjects) associées à une opération donnée"
    )
    public ApiResponse<PageResponse<EntityObjectResponse>> getByOperationId(
            @PathVariable UUID operationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return entityObjectService.getEntityObjectsByOperationId(operationId);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un EntityObject",
            description = "Supprime définitivement une entité métier (EntityObject) à partir de son identifiant"
    )
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        return entityObjectService.delete(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activer un EntityObject",
            description = "Active une entité métier (EntityObject) précédemment désactivée"
    )
    public ApiResponse<EntityObjectResponse> activate(@PathVariable UUID id) {
        return entityObjectService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Désactiver un EntityObject",
            description = "Désactive une entité métier (EntityObject) sans la supprimer"
    )
    public ApiResponse<EntityObjectResponse> deactivate(@PathVariable UUID id) {
        return entityObjectService.deactivate(id);
    }
}
