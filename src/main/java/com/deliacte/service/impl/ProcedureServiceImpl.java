package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ProcedureRequest;
import com.deliacte.dto.response.ParameterSimpleResponse;
import com.deliacte.dto.response.ProcedureResponse;
import com.deliacte.dto.response.OrganisationSimpleResponse;
import com.deliacte.entity.Organisation;
import com.deliacte.entity.Parameter;
import com.deliacte.entity.Procedure;
import com.deliacte.enums.ProcedureStatus;
import com.deliacte.enums.UserStatus;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.OrganisationRepository;
import com.deliacte.repository.ParameterRepository;
import com.deliacte.repository.ProcedureRepository;
import com.deliacte.service.ProcedureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcedureServiceImpl implements ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final OrganisationRepository organisationRepository;
    private  final ParameterRepository parameterRepository;

    @Override
    public ApiResponse<ProcedureResponse> create(ProcedureRequest request) {
        // Vérifier si le code existe déjà
        if (procedureRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            return ApiResponse.error("Une procédure avec ce code existe déjà");
        }


        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(request.getOrganisationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));
        // Gestion de la catégorie
        Parameter category = null;
        if (request.getCategoryId() != null) {
            category = parameterRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        }



        Procedure procedure = Procedure.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .processingTime(request.getProcessingTime())
                .fee(request.getFee())
                .status(ProcedureStatus.PUBLISHED)
                .organisation(organisation)
                .category(category) // <-- ajout de la catégorie
                .allowedUserStatuses(request.getAllowedUserStatuses())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .build();

        Procedure saved = procedureRepository.save(procedure);
        log.info("Procédure créée: {}", saved.getName());

        return ApiResponse.created(mapToResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ProcedureResponse> getById(UUID id) {
        Procedure procedure = procedureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));
        return ApiResponse.success(mapToResponse(procedure));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ProcedureResponse>> getAll(Pageable pageable) {
        Page<Procedure> page = procedureRepository.findByDeletedFalse(pageable);
        PageResponse<ProcedureResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> getAllTop6(Pageable pageable) {
        Page<Procedure> page = procedureRepository.findProceduresByDistinctDossierCountDesc(pageable);
        PageResponse<ProcedureResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ProcedureResponse>> getPublicProcedures(Pageable pageable) {
        Page<Procedure> page = procedureRepository.findByIsPublicTrueAndStatusAndDeletedFalse(ProcedureStatus.PUBLISHED, pageable);
        PageResponse<ProcedureResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ProcedureResponse>> getPublicProcedures(String search, Pageable pageable) {
        Page<Procedure> page = procedureRepository.searchPublicProcedures(
                search == null ? "" : search.trim(), pageable);
        PageResponse<ProcedureResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ProcedureResponse>> getByOrganisation(UUID organisationId, Pageable pageable) {
        Page<Procedure> page = procedureRepository.findByOrganisationIdAndDeletedFalse(organisationId, pageable);
        PageResponse<ProcedureResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> getByOrganisationNoPage(UUID organisationId) {

        List<ProcedureResponse> responses =
                procedureRepository.findByOrganisationIdAndDeletedFalse(organisationId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(PageResponse.of(responses));
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> findByUserId(UUID userId) {

        List<ProcedureResponse> responses =
                procedureRepository.findByUserProcedureId(userId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();


        return ApiResponse.success(PageResponse.of(responses));
    }

    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> findByUserEmail(String email) {
        return null;
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> findByResponsableProcedureUserEmail(String email) {

        List<ProcedureResponse> responses =
                procedureRepository.findByUserProcedureEmail(email)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(PageResponse.of(responses));
    }



    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> findProceduresByUserOperations(UUID id) {

        List<ProcedureResponse> responses =
                procedureRepository.findProceduresByUserOperations(id)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(PageResponse.of(responses));
    }



    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<ProcedureResponse>> findByResponsableOrganisationUserEmail(String email) {

        List<ProcedureResponse> responses =
                procedureRepository.findByUserOrganisationEmail(email)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(PageResponse.of(responses));
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ProcedureResponse>> search(String query, Pageable pageable) {
        Page<Procedure> page = procedureRepository.searchProcedures(query, pageable);
        PageResponse<ProcedureResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProcedureResponse>> getByUserStatus(UserStatus userStatus) {
        List<Procedure> procedures = procedureRepository.findByAllowedUserStatusesContainingAndStatusAndDeletedFalse(
                userStatus.name(), ProcedureStatus.PUBLISHED);
        List<ProcedureResponse> responses = procedures.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @Override
    public ApiResponse<ProcedureResponse> update(UUID id, ProcedureRequest request) {
        Procedure procedure = procedureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));

        // Vérifier le code si modifié
        if (!procedure.getCode().equals(request.getCode()) && 
            procedureRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            return ApiResponse.error("Une procédure avec ce code existe déjà");
        }
        // Gestion de la catégorie
        Parameter category = null;
        if (request.getCategoryId() != null) {
            category = parameterRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
        }
        if(category!=null){
            procedure.setCategory(category);
        }
        Organisation organisation = organisationRepository
                .findByIdAndDeletedFalse(request.getOrganisationId())
                .orElse(null);

        if (organisation != null) {
            procedure.setOrganisation(organisation);
            // Faire quelque chose avec l'organisation
        } else {
            // Organisation non trouvée, mais on continue sans erreur
            // Tu peux mettre un log ou un comportement par défaut
            System.out.println("Organisation non trouvée, continuation du processus...");
        }


        procedure.setName(request.getName());
        procedure.setCode(request.getCode());
        procedure.setDescription(request.getDescription());
        procedure.setRequirements(request.getRequirements());
        procedure.setProcessingTime(request.getProcessingTime());
        procedure.setFee(request.getFee());
        procedure.setAllowedUserStatuses(request.getAllowedUserStatuses());
        procedure.setIsPublic(request.getIsPublic());

        Procedure updated = procedureRepository.save(procedure);
        log.info("Procédure mise à jour: {}", updated.getName());

        return ApiResponse.success(mapToResponse(updated), "Procédure mise à jour avec succès");
    }


    @Override
    public ApiResponse<ProcedureResponse> updateDescription(UUID id, String description) {

        Procedure procedure = procedureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));

        if (description == null || description.trim().isEmpty()) {
            return ApiResponse.error("La description ne peut pas être vide");
        }

        // On conserve le HTML tel qu'il vient de l'éditeur riche
        procedure.setDescription(description);

        Procedure updated = procedureRepository.save(procedure);

        log.info("Description HTML mise à jour pour la procédure {}", updated.getName());

        return ApiResponse.success(mapToResponse(updated), "Description mise à jour avec succès");
    }


    @Override
    public ApiResponse<Void> delete(UUID id) {
        Procedure procedure = procedureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));

        procedure.softDelete();
        procedureRepository.save(procedure);
        log.info("Procédure supprimée: {}", procedure.getName());

        return ApiResponse.success(null, "Procédure supprimée avec succès");
    }

    @Override
    public ApiResponse<ProcedureResponse> changeStatus(UUID id, ProcedureStatus status) {
        Procedure procedure = procedureRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));

        procedure.setStatus(status);
        Procedure updated = procedureRepository.save(procedure);

        return ApiResponse.success(mapToResponse(updated), "Statut de la procédure mis à jour");
    }

    private ProcedureResponse mapToResponse(Procedure procedure) {
        OrganisationSimpleResponse orgResponse = null;
        if (procedure.getOrganisation() != null) {
            orgResponse = OrganisationSimpleResponse.builder()
                    .id(procedure.getOrganisation().getId())
                    .name(procedure.getOrganisation().getName())
                    .code(procedure.getOrganisation().getCode())
                    .logoUrl(procedure.getOrganisation().getLogoUrl())
                    .build();
        }

        ParameterSimpleResponse categoryResponse = null;

        if (procedure.getCategory() != null) {
            categoryResponse = ParameterSimpleResponse.builder()
                    .id(procedure.getCategory().getId())
                    .label(procedure.getCategory().getLabel())
                    .code(procedure.getCategory().getCode())
                    .build();
        }



        return ProcedureResponse.builder()
                .id(procedure.getId())
                .name(procedure.getName())
                .code(procedure.getCode())
                .description(procedure.getDescription())
                .requirements(procedure.getRequirements())
                .processingTime(procedure.getProcessingTime())
                .fee(procedure.getFee())
                .category(categoryResponse)
                .status(procedure.getStatus())
                .organisation(orgResponse)
                .allowedUserStatuses(procedure.getAllowedUserStatuses())
                .isPublic(procedure.getIsPublic())
                .createdAt(procedure.getCreatedAt())
                .updatedAt(procedure.getUpdatedAt())
                .build();
    }
}
