package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ChangePasswordRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.UpdateProfileRequest;
import com.deliacte.dto.request.UserRequest;
import com.deliacte.dto.response.UserProfileResponse;
import com.deliacte.dto.response.UserResponse;
import com.deliacte.enums.UserRole;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur dans le système")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        ApiResponse<UserResponse> response = userService.createUser(request);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les informations d'un utilisateur existant")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequest request) {
        ApiResponse<UserResponse> response = userService.updateUser(id, request);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur", description = "Récupère les détails d'un utilisateur par son ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        ApiResponse<UserResponse> response = userService.getUserById(id);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(response);
    }


    @GetMapping("/me")
    @Operation(summary = "Récupérer l'utilisateur connecté", description = "Retourne les informations de l'utilisateur actuellement connecté")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        // Si l'utilisateur n'est pas connecté, retourner un UserResponse vide
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.ok(ApiResponse.success(new UserResponse(), "Utilisateur non connecté"));
        }

        // Récupérer l'email ou username depuis le principal
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Récupérer l'utilisateur depuis le service
        ApiResponse<UserResponse> response = userService.getUserByEmail(email);

        // Si l'utilisateur n'existe pas côté base, retourner également un vide
        if (!response.getSuccess() || response.getData() == null) {
            return ResponseEntity.ok(ApiResponse.success(new UserResponse(), "Utilisateur non trouvé"));
        }

        return ResponseEntity.ok(response);
    }


    @PutMapping("/me/profile")
    @Operation(
            summary = "Mettre à jour mon profil",
            description = "Permet à l'utilisateur connecté de mettre à jour ses informations personnelles " +
                    "(nom, prénom, téléphone, genre, date de naissance, adresse, statut, documents d'identification...)"
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(

            @Valid @RequestBody UpdateProfileRequest request) {

        UUID userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();
        UserRole role = SecurityUtils.getCurrentUserRole();

        System.out.println(userId);
        ApiResponse<UserProfileResponse> response = userService.updateProfile(userId, request);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }


    @GetMapping("/email/{email}")
    @Operation(summary = "Récupérer un utilisateur par email", description = "Récupère les détails d'un utilisateur par son email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        ApiResponse<UserResponse> response = userService.getUserByEmail(email);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs", description = "Récupère la liste paginée de tous les utilisateurs")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Lister les utilisateurs par rôle", description = "Récupère la liste des utilisateurs ayant un rôle spécifique")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getUsersByRole(role, pageable));
    }

    @GetMapping("/organisation/{organisationId}")
    @Operation(summary = "Lister les utilisateurs par organisation", description = "Récupère la liste des utilisateurs d'une organisation")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByOrganisation(
            @PathVariable UUID organisationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getUsersByOrganisation(organisationId, pageable));
    }
    @GetMapping("/my-users")
    @Operation(summary = "Lister les utilisateurs liés à mes organisations ou créés par moi")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersOrCreatedByMe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getUsersOrCreatedByMe( pageable));
    }


    @GetMapping("/procedure/{procedureId}")
    @Operation(
            summary = "Lister les utilisateurs par procédure",
            description = "Récupère la liste des utilisateurs associés à une procédure"
    )
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByProcedure(
            @PathVariable UUID procedureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getUsersByProcedure(procedureId, pageable));
    }

    @GetMapping("/operation/{operationId}")
    @Operation(
            summary = "Lister les utilisateurs par opération",
            description = "Récupère la liste des utilisateurs associés à une opération"
    )
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByOperation(
            @PathVariable UUID operationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getUsersByOperation(operationId, pageable));
    }


    @GetMapping("/search")
    @Operation(summary = "Rechercher des utilisateurs", description = "Recherche des utilisateurs par nom, prénom ou email")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.searchUsers(q, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        ApiResponse<Void> response = userService.deleteUser(id);
        return ResponseEntity.status(response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(response);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer un utilisateur", description = "Active le compte d'un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver un utilisateur", description = "Désactive le compte d'un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PatchMapping("/{id}/password/reset")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Réinitialise le mot de passe d'un utilisateur")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable UUID id,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(userService.resetPassword(id, newPassword));
    }

    @PatchMapping("/{id}/organisations")
    @Operation(summary = "Assigner des organisations", description = "Assigne des organisations à un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> assignOrganisations(
            @PathVariable UUID id,
            @RequestBody IdListRequest ids) {
        return ResponseEntity.ok(userService.assignOrganisations(id, ids));
    }

    @PatchMapping("/{id}/procedures")
    @Operation(summary = "Assigner des procédures", description = "Assigne des procédures à un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> assignProcedures(
            @PathVariable UUID id,
            @RequestBody IdListRequest  procedureIds) {
        return ResponseEntity.ok(userService.assignProcedures(id, procedureIds));
    }

    @PatchMapping("/{id}/operations")
    @Operation(summary = "Assigner des opérations", description = "Assigne des opérations à un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> assignOperations(
            @PathVariable UUID id,
            @RequestBody IdListRequest operationIds) {
        return ResponseEntity.ok(userService.assignOperations(id, operationIds));
    }


    @PostMapping("/change-password")
    @Operation(summary = "Changer le mot de passe de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<Void>> changePassword(

            @RequestBody ChangePasswordRequest request) {

        // Récupérer l'ID de l'utilisateur connecté à partir du principal
        UUID userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();
        UserRole role = SecurityUtils.getCurrentUserRole();
        ApiResponse<Void> response = userService.changePassword(userId, request);

        return ResponseEntity.ok(response);
    }



}
