package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.ParameterRequest;
import com.deliacte.dto.response.ParameterResponse;
import com.deliacte.enums.ParameterType;
import com.deliacte.service.ParameterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/parameters")
@RequiredArgsConstructor
public class ParameterController {

    private final ParameterService parameterService;

    @PostMapping
    public ApiResponse<ParameterResponse> create(
            @Valid @RequestBody ParameterRequest request) {
        return parameterService.create(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<ParameterResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ParameterRequest request) {
        return parameterService.update(id, request);
    }

    @GetMapping("/{id}")
    public ApiResponse<ParameterResponse> getById(@PathVariable UUID id) {
        return parameterService.getById(id);
    }

    @GetMapping
    public ApiResponse<List<ParameterResponse>> getByType(
            @RequestParam ParameterType type) {
        return parameterService.getByType(type);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        return parameterService.delete(id);
    }
}
