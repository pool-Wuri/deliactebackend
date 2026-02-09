package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ParameterRequest;
import com.deliacte.dto.response.ParameterResponse;
import com.deliacte.enums.ParameterType;

import java.util.List;
import java.util.UUID;

public interface ParameterService {

    ApiResponse<ParameterResponse> create(ParameterRequest request);

    ApiResponse<ParameterResponse> update(UUID id, ParameterRequest request);

    ApiResponse<Void> delete(UUID id);

    ApiResponse<ParameterResponse> getById(UUID id);

    ApiResponse<List<ParameterResponse>> getByType(ParameterType type);
}
