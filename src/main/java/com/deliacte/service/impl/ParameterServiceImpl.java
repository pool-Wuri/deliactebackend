package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ParameterRequest;
import com.deliacte.dto.response.ParameterResponse;
import com.deliacte.entity.Parameter;
import com.deliacte.enums.ParameterType;
import com.deliacte.repository.ParameterRepository;
import com.deliacte.service.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParameterServiceImpl implements ParameterService {

    private final ParameterRepository parameterRepository;

    @Override
    public ApiResponse<ParameterResponse> create(ParameterRequest request) {

        parameterRepository.findByTypeAndCode(request.getType(), request.getCode())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Un paramètre avec ce code existe déjà pour ce type");
                });

        Parameter parameter = Parameter.builder()
                .type(request.getType())
                .code(request.getCode())
                .label(request.getLabel())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        return ApiResponse.created(mapToResponse(parameterRepository.save(parameter)));
    }

    @Override
    public ApiResponse<ParameterResponse> update(UUID id, ParameterRequest request) {

        Parameter parameter = parameterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paramètre introuvable"));

        parameter.setCode(request.getCode());
        parameter.setLabel(request.getLabel());
        parameter.setDescription(request.getDescription());
        parameter.setIsActive(request.getIsActive());
        parameter.setDisplayOrder(request.getDisplayOrder());

        return ApiResponse.success(mapToResponse(parameterRepository.save(parameter)));
    }

    @Override
    public ApiResponse<Void> delete(UUID id) {

        if (!parameterRepository.existsById(id)) {
            return ApiResponse.notFound("Paramètre introuvable");
        }

        parameterRepository.deleteById(id);
        return ApiResponse.success(null, "Paramètre supprimé avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ParameterResponse> getById(UUID id) {

        return parameterRepository.findById(id)
                .map(p -> ApiResponse.success(mapToResponse(p)))
                .orElse(ApiResponse.notFound("Paramètre introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ParameterResponse>> getByType(ParameterType type) {

        List<ParameterResponse> responses = parameterRepository
                .findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    private ParameterResponse mapToResponse(Parameter p) {
        return ParameterResponse.builder()
                .id(p.getId())
                .type(p.getType())
                .code(p.getCode())
                .label(p.getLabel())
                .description(p.getDescription())
                .isActive(p.getIsActive())
                .displayOrder(p.getDisplayOrder())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
