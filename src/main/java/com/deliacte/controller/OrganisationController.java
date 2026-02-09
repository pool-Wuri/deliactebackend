package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.OrganisationRequest;
import com.deliacte.dto.response.OrganisationResponse;
import com.deliacte.service.OrganisationService;
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
@RequestMapping("/v1/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "API de gestion des organisations")
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping
    @Operation(summary = "Créer une organisation")
    public ResponseEntity<ApiResponse<OrganisationResponse>> createOrganisation(@Valid @RequestBody OrganisationRequest request) {
        ApiResponse<OrganisationResponse> response = organisationService.create(request);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une organisation")
    public ResponseEntity<ApiResponse<OrganisationResponse>> updateOrganisation(
            @PathVariable UUID id,
            @Valid @RequestBody OrganisationRequest request) {
        ApiResponse<OrganisationResponse> response = organisationService.update(id, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une organisation par ID")
    public ResponseEntity<ApiResponse<OrganisationResponse>> getOrganisationById(@PathVariable UUID id) {
        return ResponseEntity.ok(organisationService.getById(id));
    }
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Lister les organisations d'un utilisateur",
            description = "Récupère toutes les organisations associées à un utilisateur donné"
    )
    public ResponseEntity<ApiResponse<PageResponse<OrganisationResponse>>> getOrganisationsByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(organisationService.findByUserId(userId));
    }
    @GetMapping("/me")
    @Operation(
            summary = "Lister les organisations de l'utilisateur connecté",
            description = "Récupère toutes les organisations associées à l'utilisateur authentifié"
    )
    public ResponseEntity<ApiResponse<PageResponse<OrganisationResponse>>> getMyOrganisations() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return ResponseEntity.ok(organisationService.findByUserEmail(email));
    }


    @GetMapping
    @Operation(summary = "Lister toutes les organisations")
    public ResponseEntity<ApiResponse<PageResponse<OrganisationResponse>>> getAllOrganisations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(organisationService.getAll(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les organisations sans pagination")
    public  ResponseEntity<ApiResponse<PageResponse<OrganisationResponse>>> getAllOrganisationsNoPage() {
        return ResponseEntity.ok(organisationService.getAllWithoutPagination());
    }



    @GetMapping("/search")
    @Operation(summary = "Rechercher des organisations")
    public ResponseEntity<ApiResponse<PageResponse<OrganisationResponse>>> searchOrganisations(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(organisationService.search(q, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une organisation")
    public ResponseEntity<ApiResponse<Void>> deleteOrganisation(@PathVariable UUID id) {
        ApiResponse<Void> response = organisationService.delete(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Activer/Désactiver une organisation")
    public ResponseEntity<ApiResponse<OrganisationResponse>> toggleActive(@PathVariable UUID id) {
        ApiResponse<OrganisationResponse> response = organisationService.toggleActive(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
