package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.OrganisationRequest;
import com.deliacte.dto.response.OrganisationResponse;
import com.deliacte.dto.response.ParameterSimpleResponse;
import com.deliacte.entity.Organisation;
import com.deliacte.entity.Parameter;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.OrganisationRepository;
import com.deliacte.repository.ParameterRepository;
import com.deliacte.service.OrganisationService;
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
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;
    private  final ParameterRepository parameterRepository;

    @Override
    public ApiResponse<OrganisationResponse> create(OrganisationRequest request) {
        // Vérifier si le code existe déjà
        if (organisationRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            return ApiResponse.error("Une organisation avec ce code existe déjà");
        }

        Organisation organisation = Organisation.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .address(request.getAddress())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .logoUrl(request.getLogoUrl())
                .isActive(true)
                .build();

        // Gestion de l'organisation parente
        if (request.getParentId() != null) {
            Organisation parent = organisationRepository.findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organisation parente non trouvée"));
            organisation.setParent(parent);
        }
        if (request.getOrganisationTypeId() != null) {
            Parameter typeOrganisation = parameterRepository.findById(request.getOrganisationTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'organisation non trouvé"));
            organisation.setOrganisationType(typeOrganisation);
        }

        Organisation saved = organisationRepository.save(organisation);
        log.info("Organisation créée: {}", saved.getName());

        return ApiResponse.created(mapToResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrganisationResponse> getById(UUID id) {
        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));
        return ApiResponse.success(mapToResponse(organisation));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<OrganisationResponse>> getAll(Pageable pageable) {
        Page<Organisation> page = organisationRepository.findByDeletedFalse(pageable);
        PageResponse<OrganisationResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OrganisationResponse>> findByUserId(UUID userId) {

        List<Organisation> organisations =
                organisationRepository.findOrganisationsWithProceduresByUserId(userId);

        PageResponse<OrganisationResponse> response =
                PageResponse.of(
                        organisations.stream()
                                .map(this::mapToResponse)
                                .toList()
                );

        return ApiResponse.success(response);
    }
    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OrganisationResponse>> findByUserEmail(String email) {

        List<Organisation> organisations =
                organisationRepository.findOrganisationsWithProceduresByUserEmail(email);

        PageResponse<OrganisationResponse> response =
                PageResponse.of(
                        organisations.stream()
                                .map(this::mapToResponse)
                                .toList()
                );

        return ApiResponse.success(response);
    }



    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OrganisationResponse>> getAllWithoutPagination() {
        List<Organisation> organisations = organisationRepository.findAllByDeletedFalse();
        List<OrganisationResponse> responses = organisations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(PageResponse.of(responses));
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<OrganisationResponse>> search(String query, Pageable pageable) {
        Page<Organisation> page = organisationRepository.searchOrganisations(query, pageable);
        PageResponse<OrganisationResponse> response = PageResponse.of(page.map(this::mapToResponse));
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<OrganisationResponse> update(UUID id, OrganisationRequest request) {
        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        // Vérifier le code si modifié
        if (!organisation.getCode().equals(request.getCode()) && 
            organisationRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            return ApiResponse.error("Une organisation avec ce code existe déjà");
        }


        organisation.setName(request.getName());
        organisation.setCode(request.getCode());
        organisation.setDescription(request.getDescription());
        organisation.setAddress(request.getAddress());
        organisation.setTelephone(request.getTelephone());
        organisation.setEmail(request.getEmail());
        organisation.setWebsite(request.getWebsite());
        organisation.setLogoUrl(request.getLogoUrl());

        if (request.getParentId() != null) {
            Organisation parent = organisationRepository.findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organisation parente non trouvée"));
            organisation.setParent(parent);
        } else {
            organisation.setParent(null);
        }

        if (request.getOrganisationTypeId() != null) {
            Parameter typeOrganisation = parameterRepository.findById(request.getOrganisationTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'organisation non trouvé"));
            organisation.setOrganisationType(typeOrganisation);
        }

        Organisation updated = organisationRepository.save(organisation);
        log.info("Organisation mise à jour: {}", updated.getName());

        return ApiResponse.success(mapToResponse(updated), "Organisation mise à jour avec succès");
    }


    @Override
    public ApiResponse<Void> delete(UUID id) {
        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        organisation.softDelete();
        organisationRepository.save(organisation);
        log.info("Organisation supprimée: {}", organisation.getName());

        return ApiResponse.success(null, "Organisation supprimée avec succès");
    }

    @Override
    public ApiResponse<OrganisationResponse> toggleActive(UUID id) {
        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée"));

        organisation.setIsActive(!organisation.getIsActive());
        Organisation updated = organisationRepository.save(organisation);

        String message = updated.getIsActive() ? "Organisation activée" : "Organisation désactivée";
        return ApiResponse.success(mapToResponse(updated), message);
    }

    private OrganisationResponse mapToResponse(Organisation organisation) {
        return OrganisationResponse.builder()
                .id(organisation.getId())
                .name(organisation.getName())
                .code(organisation.getCode())
                .typeOrganisation(
                        organisation.getOrganisationType() != null
                                ? ParameterSimpleResponse.builder()
                                .id(organisation.getOrganisationType().getId())
                                .label(organisation.getOrganisationType().getLabel())
                                .code(organisation.getOrganisationType().getCode())
                                .build()
                                : null
                )

                .description(organisation.getDescription())
                .address(organisation.getAddress())
                .telephone(organisation.getTelephone())
                .email(organisation.getEmail())
                .website(organisation.getWebsite())
                .logoUrl(organisation.getLogoUrl())
                .isActive(organisation.getIsActive())
                .parentId(organisation.getParent() != null ? organisation.getParent().getId() : null)
                .parentName(organisation.getParent() != null ? organisation.getParent().getName() : null)
                .createdAt(organisation.getCreatedAt())
                .updatedAt(organisation.getUpdatedAt())
                .build();
    }
}
