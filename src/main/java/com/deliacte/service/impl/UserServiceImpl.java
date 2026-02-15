package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ChangePasswordRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.UserRequest;
import com.deliacte.dto.response.OperationSimpleResponse;
import com.deliacte.dto.response.OrganisationSimpleResponse;
import com.deliacte.dto.response.ProcedureSimpleResponse;
import com.deliacte.dto.response.UserResponse;
import com.deliacte.entity.Operation;
import com.deliacte.entity.Organisation;
import com.deliacte.entity.Procedure;
import com.deliacte.entity.User;
import com.deliacte.enums.UserRole;
import com.deliacte.exception.BadRequestException;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.OperationRepository;
import com.deliacte.repository.OrganisationRepository;
import com.deliacte.repository.ProcedureRepository;
import com.deliacte.repository.UserRepository;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.UserService;
import com.deliacte.utils.EmailService;
import com.deliacte.utils.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final ProcedureRepository procedureRepository;
    private final OperationRepository operationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Override
    public ApiResponse<UserResponse> createUser(UserRequest request) {
        // Vérification de l'existence de l'utilisateur

        String email = request.getEmail() == null
                ? null
                : request.getEmail().trim();
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            return ApiResponse.error("Un utilisateur avec cet email existe déjà");
        }

        // Récupérer l'utilisateur connecté (créateur)
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User creator = null;
        if (currentUserId != null) {
            creator = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur créateur non trouvé"));
        }

        // --- Gestion du mot de passe ---
        String rawPassword = request.getPassword();

        // Générer un mot de passe si vide
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = PasswordGenerator.generate(12); // génération de mot de passe rigide
            log.info("Mot de passe généré automatiquement pour l'utilisateur {} : {}", email, rawPassword);
        }

        // Création de l'utilisateur
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .telephone(request.getTelephone())
                .password(passwordEncoder.encode(rawPassword))
                .role(request.getRole() != null ? request.getRole() : UserRole.CITOYEN)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        user.setCreatedBy(creator);

        // Affectation des organisations
        if (request.getOrganisationIds() != null && !request.getOrganisationIds().isEmpty()) {
            Set<Organisation> organisations = request.getOrganisationIds().stream()
                    .map(orgId -> organisationRepository.findByIdAndDeletedFalse(orgId)
                            .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée: " + orgId)))
                    .collect(Collectors.toSet());
            user.setOrganisations(organisations);
        }


        // Sauvegarde de l'utilisateur
        User savedUser = userRepository.save(user);
        try {
            emailService.sendAccountCreationEmail(email, user.getFirstName(), user.getLastName(), rawPassword);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de vérification: {}", e.getMessage());
            // On ne bloque pas l'inscription si l'email échoue
        }


        // Log du créateur et du nouvel utilisateur
        if (creator != null) {
            log.info("Utilisateur '{}' créé par '{}'", savedUser.getEmail(), creator.getEmail());
        } else {
            log.info("Utilisateur '{}' créé (créateur non identifié)", savedUser.getEmail());
        }

        return ApiResponse.created(mapToResponse(savedUser));
    }


    @Override
    public ApiResponse<UserResponse> updateUser(UUID id, UserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            return ApiResponse.error("Un utilisateur avec cet email existe déjà");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour: {}", updatedUser.getEmail());
        return ApiResponse.success(mapToResponse(updatedUser), "Utilisateur mis à jour avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserResponse> getUserById(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return ApiResponse.success(mapToResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserResponse> getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return ApiResponse.success(mapToResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<UserResponse>> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findByDeletedFalse(pageable);
        Page<UserResponse> responsePage = users.map(this::mapToResponse);
        return ApiResponse.success(PageResponse.of(responsePage));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<UserResponse>> getUsersByRole(UserRole role, Pageable pageable) {
        Page<User> users = userRepository.findByRoleAndDeletedFalse(role, pageable);
        Page<UserResponse> responsePage = users.map(this::mapToResponse);
        return ApiResponse.success(PageResponse.of(responsePage));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<UserResponse>> getUsersByOrganisation(UUID organisationId, Pageable pageable) {
        Page<User> users = userRepository.findByOrganisationId(organisationId, pageable);
        Page<UserResponse> responsePage = users.map(this::mapToResponse);
        return ApiResponse.success(PageResponse.of(responsePage));
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<UserResponse>> getUsersByProcedure(
            UUID procedureId,
            Pageable pageable   // conservé pour cohérence API
    ) {
        List<UserResponse> responses =
                userRepository.findByProcedureId(procedureId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(PageResponse.of(responses));
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<UserResponse>> getUsersByOperation(
            UUID operationId,
            Pageable pageable
    ) {
        List<UserResponse> responses =
                userRepository.findByOperationId(operationId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(PageResponse.of(responses));
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<UserResponse>> searchUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(search, pageable);
        Page<UserResponse> responsePage = users.map(this::mapToResponse);
        return ApiResponse.success(PageResponse.of(responsePage));
    }

    @Override
    public ApiResponse<Void> deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.softDelete();
        userRepository.save(user);
        log.info("Utilisateur supprimé: {}", user.getEmail());
        return ApiResponse.success(null, "Utilisateur supprimé avec succès");
    }

    @Override
    public ApiResponse<UserResponse> activateUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        User updatedUser = userRepository.save(user);
        return ApiResponse.success(mapToResponse(updatedUser), "Utilisateur activé avec succès");
    }

    @Override
    public ApiResponse<UserResponse> deactivateUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setEnabled(false);
        User updatedUser = userRepository.save(user);
        return ApiResponse.success(mapToResponse(updatedUser), "Utilisateur désactivé avec succès");
    }

    @Override
    public ApiResponse<Void> changePassword(UUID id, String oldPassword, String newPassword) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ApiResponse.error("L'ancien mot de passe est incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ApiResponse.success(null, "Mot de passe modifié avec succès");
    }

    @Override
    public ApiResponse<Void> resetPassword(UUID id, String newPassword) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setConnectionFailureCount(0);
        user.setAccountNonLocked(true);
        userRepository.save(user);
        return ApiResponse.success(null, "Mot de passe réinitialisé avec succès");
    }

    @Override
    public ApiResponse<UserResponse> assignOrganisations(UUID userId, IdListRequest request) {

        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            throw new IllegalArgumentException("La liste des identifiants ne peut pas être vide");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Set<Organisation> organisations = request.getIds().stream()
                .map(orgId -> organisationRepository.findByIdAndDeletedFalse(orgId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Organisation non trouvée : " + orgId)))
                .collect(Collectors.toSet());

        user.setOrganisations(organisations);

        User updatedUser = userRepository.save(user);

        return ApiResponse.success(
                mapToResponse(updatedUser),
                "Organisations assignées avec succès"
        );
    }


    @Override
    public ApiResponse<UserResponse> assignProcedures(UUID userId, IdListRequest ids) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Set<Procedure> procedures = new HashSet<>();
        for (UUID procId : ids.getIds()) {
            Procedure proc = procedureRepository.findByIdAndDeletedFalse(procId)
                    .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));
            procedures.add(proc);
        }
        user.setProcedures(procedures);
        User updatedUser = userRepository.save(user);
        return ApiResponse.success(mapToResponse(updatedUser), "Procédures assignées avec succès");
    }

    @Override
    public ApiResponse<UserResponse> assignOperations(UUID userId, IdListRequest operationIds) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Set<Operation> operations = new HashSet<>();
        for (UUID opId : operationIds.getIds()) {
            Operation op = operationRepository.findByIdAndDeletedFalse(opId)
                    .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));
            operations.add(op);
        }
        user.setOperations(operations);
        User updatedUser = userRepository.save(user);
        try {
            //  emailService.sendOperationsAssignedEmail(user, operations);
        } catch (Exception e) { // attrape toutes les exceptions
            log.error("Erreur lors de l'envoi de l'email d'assignation des opérations pour l'utilisateur {}: {}",
                    user.getEmail(), e.getMessage(), e);
            // Optionnel : tu peux décider de ne pas bloquer l'exécution si l'email échoue
        }

        return ApiResponse.success(mapToResponse(updatedUser), "Opérations assignées avec succès");
    }


    @Override
    public ApiResponse<Void> changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur non trouvé"));

        // Vérifier ancien mot de passe
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }

        // Vérifier que le nouveau mot de passe et la confirmation sont identiques
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("La confirmation du mot de passe ne correspond pas");
        }

        // Enregistrer le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.<Void>success(null, "Mot de passe modifié avec succès");
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<UserResponse>> getUsersOrCreatedByMe(Pageable pageable) {
        // Récupérer l'ID de l'utilisateur connecté depuis SecurityContext
        UUID myUserId = SecurityUtils.getCurrentUserId(); // ta méthode pour récupérer l'ID courant

        // Appel à ton repository JPQL combiné
        Page<User> users = userRepository.findUsersLinkedToMyOrganisationsOrCreatedByMe(myUserId, pageable);

        // Mapper en UserResponse
        Page<UserResponse> responsePage = users.map(this::mapToResponse);

        return ApiResponse.success(PageResponse.of(responsePage));
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<UserResponse>> findByOperationId(UUID operationId) {

        // Appel à ton repository JPQL combiné
        List<User> users = userRepository.findByOperationId(operationId);
        // Mapper la liste en UserResponse
        List<UserResponse> content = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());


        return ApiResponse.success(PageResponse.of(content));
    }


    private UserResponse mapToResponse(User user) {
        Set<OrganisationSimpleResponse> organisations = user.getOrganisations() != null ?
                user.getOrganisations().stream()
                        .map(org -> OrganisationSimpleResponse.builder()
                                .id(org.getId())
                                .name(org.getName())
                                .code(org.getCode())
                                .logoUrl(org.getLogoUrl())
                                .build())
                        .collect(Collectors.toSet()) : null;

        Set<ProcedureSimpleResponse> procedures = user.getProcedures() != null ?
                user.getProcedures().stream()
                        .map(proc -> ProcedureSimpleResponse.builder()
                                .id(proc.getId())
                                .name(proc.getName())
                                .code(proc.getCode())
                                .status(proc.getStatus())
                                .isPublic(proc.getIsPublic())
                                .build())
                        .collect(Collectors.toSet()) : null;

        Set<OperationSimpleResponse> operations = user.getOperations() != null
                ? user.getOperations().stream()
                .map(op -> OperationSimpleResponse.builder()
                        .id(op.getId())
                        .name(op.getName())
                        .isActive(op.getIsActive())
                        .hasPayment(op.getHasPayment())
                        .prix(op.getFee())
                        .isFirstOperation(op.getIsFirstOperation())
                        .isLastOperation(op.getIsLastOperation())
                        .build()
                )
                .collect(Collectors.toSet())
                : null;


        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .userStatus(user.getUserStatus())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .connectionCount(user.getConnectionCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .organisations(organisations)
                .procedures(procedures)
                .operations(operations)
                .build();
    }
}
