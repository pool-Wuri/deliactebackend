package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.EntityObjectRequest;
import com.deliacte.dto.response.EntityObjectResponse;
import com.deliacte.entity.EntityObject;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.EntityObjectRepository;
import com.deliacte.service.EntityObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EntityObjectServiceImpl implements EntityObjectService {

    private final EntityObjectRepository entityObjectRepository;

    @Override
    public ApiResponse<EntityObjectResponse> create(EntityObjectRequest request) {
        EntityObject entity = EntityObject.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        entity = entityObjectRepository.save(entity);
        return ApiResponse.success(mapToResponse(entity), "EntityObject créé avec succès");
    }

    @Override
    public ApiResponse<EntityObjectResponse> update(UUID id, EntityObjectRequest request) {
        EntityObject entity = entityObjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntityObject non trouvé"));

        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        entity = entityObjectRepository.save(entity);
        return ApiResponse.success(mapToResponse(entity), "EntityObject mis à jour avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<EntityObjectResponse> getById(UUID id) {
        EntityObject entity = entityObjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntityObject non trouvé"));
        return ApiResponse.success(mapToResponse(entity), "EntityObject récupéré avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<EntityObjectResponse>> getAll(Pageable pageable) {
        Page<EntityObject> page = entityObjectRepository.findAll(pageable);

        PageResponse<EntityObjectResponse> response = PageResponse.<EntityObjectResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return ApiResponse.success(response, "Liste des EntityObjects récupérée avec succès");
    }
    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<EntityObjectResponse>>
    getEntityObjectsByOperationId(UUID operationId) {

        List<EntityObject> entityObjects =entityObjectRepository.findEntityObjectsByOperationId(operationId);

        List<EntityObjectResponse> responses = entityObjects.stream()
                .map(this::mapToResponse)
                .toList();

        PageResponse<EntityObjectResponse> pageResponse =
                PageResponse.of(responses);

        return ApiResponse.success(
                pageResponse,
                "Liste des EntityObjects liés à l'opération récupérée avec succès"
        );
    }




    @Override
    public ApiResponse<Void> delete(UUID id) {
        EntityObject entity = entityObjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntityObject non trouvé"));
        entityObjectRepository.delete(entity);
        return ApiResponse.<Void>success(null, "EntityObject supprimé avec succès");
    }

    @Override
    public ApiResponse<EntityObjectResponse> activate(UUID id) {
        EntityObject entity = entityObjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntityObject non trouvé"));
        entity.setIsActive(true);
        entity = entityObjectRepository.save(entity);
        return ApiResponse.success(mapToResponse(entity), "EntityObject activé avec succès");
    }

    @Override
    public ApiResponse<EntityObjectResponse> deactivate(UUID id) {
        EntityObject entity = entityObjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntityObject non trouvé"));
        entity.setIsActive(false);
        entity = entityObjectRepository.save(entity);
        return ApiResponse.success(mapToResponse(entity), "EntityObject désactivé avec succès");
    }

    private EntityObjectResponse mapToResponse(EntityObject entity) {
        return EntityObjectResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
