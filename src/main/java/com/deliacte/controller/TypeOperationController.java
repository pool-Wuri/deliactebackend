package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.TypeOperationRequest;
import com.deliacte.dto.response.TypeOperationResponse;
import com.deliacte.service.TypeOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/type-operations")
@RequiredArgsConstructor
@Tag(name = "Types d'opération", description = "Gestion des types d'opération")
public class TypeOperationController {
    
    private final TypeOperationService typeOperationService;
    
    @PostMapping
    @Operation(summary = "Créer un type d'opération", description = "Crée un nouveau type d'opération")
    public ResponseEntity<ApiResponse<TypeOperationResponse>> create(@Valid @RequestBody TypeOperationRequest request) {
        return ResponseEntity.ok(typeOperationService.create(request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un type d'opération", description = "Met à jour un type d'opération existant")
    public ResponseEntity<ApiResponse<TypeOperationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TypeOperationRequest request) {
        return ResponseEntity.ok(typeOperationService.update(id, request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un type d'opération", description = "Récupère un type d'opération par son ID")
    public ResponseEntity<ApiResponse<TypeOperationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(typeOperationService.getById(id));
    }
    
    @GetMapping

    @Operation(summary = "Lister les types d'opération", description = "Récupère la liste paginée des types d'opération")
    public ResponseEntity<ApiResponse<PageResponse<TypeOperationResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "libelle") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(typeOperationService.getAll(pageable));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Lister les types d'opération actifs", description = "Récupère tous les types d'opération actifs")
    public ResponseEntity<ApiResponse<List<TypeOperationResponse>>> getAllActive() {
        return ResponseEntity.ok(typeOperationService.getAllActive());
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un type d'opération", description = "Supprime un type d'opération (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(typeOperationService.delete(id));
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer un type d'opération", description = "Active un type d'opération")
    public ResponseEntity<ApiResponse<TypeOperationResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(typeOperationService.activate(id));
    }
    
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver un type d'opération", description = "Désactive un type d'opération")
    public ResponseEntity<ApiResponse<TypeOperationResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(typeOperationService.deactivate(id));
    }
}
