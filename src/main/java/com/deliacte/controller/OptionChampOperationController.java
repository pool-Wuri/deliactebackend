package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OptionChampOperationRequest;
import com.deliacte.dto.response.OptionChampOperationResponse;
import com.deliacte.service.OptionChampOperationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/option-champ-operations")
@RequiredArgsConstructor
public class OptionChampOperationController {

    private final OptionChampOperationService optionChampOperationService;

    // ========================= CREATE =========================

    @PostMapping
    @Operation(summary = "Créer une option pour un champ d’opération")
    public ResponseEntity<ApiResponse<OptionChampOperationResponse>> create(
            @Valid @RequestBody OptionChampOperationRequest request) {

        return ResponseEntity.ok(optionChampOperationService.create(request));
    }

    // ========================= UPDATE =========================

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une option")
    public ResponseEntity<ApiResponse<OptionChampOperationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OptionChampOperationRequest request) {

        return ResponseEntity.ok(optionChampOperationService.update(id, request));
    }

    // ========================= DELETE =========================

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une option")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(optionChampOperationService.delete(id));
    }

    // ========================= GET BY ID =========================

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une option par son ID")
    public ResponseEntity<ApiResponse<OptionChampOperationResponse>> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(optionChampOperationService.getById(id));
    }

    // ========================= GET BY CHAMP OPERATION =========================

    @GetMapping("/champ-operation/{champOperationId}")
    @Operation(summary = "Lister les options d’un ChampOperation (ordonnées)")
    public ResponseEntity<ApiResponse<List<OptionChampOperationResponse>>> getByChampOperation(
            @PathVariable UUID champOperationId) {

        return ResponseEntity.ok(
                optionChampOperationService.getByChampOperation(champOperationId)
        );
    }

    // ========================= SET DEFAULT =========================

    @PatchMapping("/{id}/default")
    @Operation(
            summary = "Définir une option comme valeur par défaut",
            description = "Met cette option comme valeur par défaut et retire le défaut des autres options du même ChampOperation"
    )
    public ResponseEntity<ApiResponse<OptionChampOperationResponse>> setDefault(
            @PathVariable UUID id) {

        return ResponseEntity.ok(optionChampOperationService.setDefault(id));
    }

    // ========================= REORDER =========================

    @PatchMapping("/reorder")
    @Operation(
            summary = "Réordonner les options",
            description = "Met à jour le champ ordre selon l’ordre des IDs reçus (à partir de 1)"
    )
    public ResponseEntity<ApiResponse<Void>> reorderOptions(
            @Valid @RequestBody IdListRequest request) {

        return ResponseEntity.ok(
                optionChampOperationService.reorderOptions(request)
        );
    }


    
}
