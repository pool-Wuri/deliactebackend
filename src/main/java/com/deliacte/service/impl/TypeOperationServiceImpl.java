package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.TypeOperationRequest;
import com.deliacte.dto.response.TypeOperationResponse;
import com.deliacte.entity.TypeOperation;
import com.deliacte.exception.BadRequestException;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.TypeOperationRepository;
import com.deliacte.service.TypeOperationService;
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
public class TypeOperationServiceImpl implements TypeOperationService {
    
    private final TypeOperationRepository typeOperationRepository;
    
    @Override
    public ApiResponse<TypeOperationResponse> create(TypeOperationRequest request) {
        // Vérifier si le code existe déjà
        if (typeOperationRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BadRequestException("Un type d'opération avec ce code existe déjà");
        }
        
        TypeOperation typeOperation = TypeOperation.builder()
                .code(request.getCode())
                .libelle(request.getLibelle())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        
        typeOperation = typeOperationRepository.save(typeOperation);
        
        return ApiResponse.success(mapToResponse(typeOperation), "Type d'opération créé avec succès");
    }
    
    @Override
    public ApiResponse<TypeOperationResponse> update(UUID id, TypeOperationRequest request) {
        TypeOperation typeOperation = typeOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
        
        // Vérifier si le nouveau code existe déjà pour un autre type
        if (!typeOperation.getCode().equals(request.getCode()) && 
            typeOperationRepository.existsByCodeAndDeletedFalse(request.getCode())) {
            throw new BadRequestException("Un type d'opération avec ce code existe déjà");
        }
        
        typeOperation.setCode(request.getCode());
        typeOperation.setLibelle(request.getLibelle());
        typeOperation.setDescription(request.getDescription());
        if (request.getActive() != null) {
            typeOperation.setActive(request.getActive());
        }
        
        typeOperation = typeOperationRepository.save(typeOperation);
        
        return ApiResponse.success(mapToResponse(typeOperation), "Type d'opération mis à jour avec succès");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<TypeOperationResponse> getById(UUID id) {
        TypeOperation typeOperation = typeOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
        
        return ApiResponse.success(mapToResponse(typeOperation), "Type d'opération récupéré avec succès");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<TypeOperationResponse>> getAll(Pageable pageable) {
        Page<TypeOperation> page = typeOperationRepository.findAllByDeletedFalse(pageable);
        
        PageResponse<TypeOperationResponse> response = PageResponse.<TypeOperationResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        
        return ApiResponse.success(response, "Liste des types d'opération récupérée avec succès");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TypeOperationResponse>> getAllActive() {
        List<TypeOperation> types = typeOperationRepository.findByActiveAndDeletedFalse(true);
        
        List<TypeOperationResponse> responses = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses, "Types d'opération actifs récupérés avec succès");
    }
    
    @Override
    public ApiResponse<Void> delete(UUID id) {
        TypeOperation typeOperation = typeOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
        
        typeOperation.setDeleted(true);
        typeOperationRepository.save(typeOperation);
        
        return ApiResponse.<Void>success(null, "Type d'opération supprimé avec succès");
    }
    
    @Override
    public ApiResponse<TypeOperationResponse> activate(UUID id) {
        TypeOperation typeOperation = typeOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
        
        typeOperation.setActive(true);
        typeOperation = typeOperationRepository.save(typeOperation);
        
        return ApiResponse.success(mapToResponse(typeOperation), "Type d'opération activé avec succès");
    }
    
    @Override
    public ApiResponse<TypeOperationResponse> deactivate(UUID id) {
        TypeOperation typeOperation = typeOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
        
        typeOperation.setActive(false);
        typeOperation = typeOperationRepository.save(typeOperation);
        
        return ApiResponse.success(mapToResponse(typeOperation), "Type d'opération désactivé avec succès");
    }
    
    private TypeOperationResponse mapToResponse(TypeOperation typeOperation) {
        return TypeOperationResponse.builder()
                .id(typeOperation.getId())
                .code(typeOperation.getCode())
                .libelle(typeOperation.getLibelle())
                .description(typeOperation.getDescription())
                .active(typeOperation.getActive())
                .createdAt(typeOperation.getCreatedAt())
                .updatedAt(typeOperation.getUpdatedAt())
                .build();
    }
}
