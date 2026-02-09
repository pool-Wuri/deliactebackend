package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ChampOperationRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OptionChampOperationRequest;
import com.deliacte.dto.response.ChampOperationResponse;
import com.deliacte.dto.response.OptionChampOperationResponse;

import java.util.List;
import java.util.UUID;

public interface OptionChampOperationService {

    ApiResponse<OptionChampOperationResponse> create(OptionChampOperationRequest request);

    ApiResponse<OptionChampOperationResponse> update(UUID id, OptionChampOperationRequest request);

    ApiResponse<Void> delete(UUID id);

    ApiResponse<OptionChampOperationResponse> getById(UUID id);

    ApiResponse<List<OptionChampOperationResponse>> getByChampOperation(UUID champOperationId);

    ApiResponse<OptionChampOperationResponse> setDefault(UUID optionId);

    ApiResponse<Void> reorderOptions(IdListRequest request);

}
