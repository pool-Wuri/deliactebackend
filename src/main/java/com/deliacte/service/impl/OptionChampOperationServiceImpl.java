package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OptionChampOperationRequest;
import com.deliacte.dto.response.OptionChampOperationResponse;
import com.deliacte.entity.ChampOperation;
import com.deliacte.entity.OptionChampOperation;
import com.deliacte.repository.ChampOperationRepository;

import com.deliacte.repository.OptionChampOperationRepository;
import com.deliacte.service.OptionChampOperationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionChampOperationServiceImpl implements OptionChampOperationService {

    private final OptionChampOperationRepository optionRepository;
    private final ChampOperationRepository champOperationRepository;



    @Override
    public ApiResponse<OptionChampOperationResponse> create(OptionChampOperationRequest request) {
        ChampOperation champOperation = champOperationRepository.findById(request.getChampOperationId())
                .orElseThrow(() -> new RuntimeException("ChampOperation introuvable"));

        // ⚠️ Si l'option est définie comme default → retirer le default des autres
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            champOperationRepository.unsetDefaultByChampOperation(champOperation.getId());
        }

        OptionChampOperation option = OptionChampOperation.builder()
                .label(request.getLabel())
                .value(request.getValue())
                .ordre(request.getOrdre())
                .orderIndex(request.getOrderIndex())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .champOperation(champOperation)
               .build();

        optionRepository.save(option);

        return ApiResponse.success(mapToResponse(option), "Option créée avec succès");
    }

    @Override
    public ApiResponse<OptionChampOperationResponse> update(UUID id, OptionChampOperationRequest request) {
        OptionChampOperation option = optionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Option introuvable"));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            champOperationRepository.unsetDefaultByChampOperation(option.getChampOperation().getId());
        }

        option.setLabel(request.getLabel());
        option.setValue(request.getValue());
        option.setOrdre(request.getOrdre());
        option.setOrderIndex(request.getOrderIndex());
        option.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        return ApiResponse.success(mapToResponse(option), "Option mise à jour");
    }

    @Override
    public ApiResponse<Void> delete(UUID id) {
        optionRepository.deleteById(id);
        return ApiResponse.success(null, "Option supprimée");
    }

    @Override
    public ApiResponse<OptionChampOperationResponse> getById(UUID id) {
        return optionRepository.findById(id)
                .map(o -> ApiResponse.success(mapToResponse(o)))
                .orElse(ApiResponse.error("Option introuvable"));
    }

    @Override
    public ApiResponse<List<OptionChampOperationResponse>> getByChampOperation(UUID champOperationId) {
        List<OptionChampOperationResponse> responses =
                optionRepository.findByChampOperationIdOrderByOrderIndexAsc(champOperationId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(responses);
    }

    /**
     * Définir UNE option comme valeur par défaut
     */
    @Override
    @Transactional
    public ApiResponse<OptionChampOperationResponse> setDefault(UUID optionId) {

        OptionChampOperation option = optionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option introuvable"));

        // Retirer le default des autres
        List<OptionChampOperation> options =
                optionRepository.findByChampOperationId(option.getChampOperation().getId());

        for (OptionChampOperation o : options) {
            o.setIsDefault(false);
        }

        option.setIsDefault(true);

        optionRepository.saveAll(options); // 🔒 clair + sûr
        optionRepository.save(option);

        return ApiResponse.success(
                mapToResponse(option),
                "Option définie comme valeur par défaut"
        );
    }


    @Override
    public ApiResponse<Void> reorderOptions(IdListRequest request) {

        List<UUID> ids = request.getIds();

        List<OptionChampOperation> options =
                optionRepository.findByIdIn(ids);

        if (options.size() != ids.size()) {
            throw new RuntimeException("Certaines options sont introuvables");
        }

        // Map pour accès rapide
        Map<UUID, OptionChampOperation> optionMap = options.stream()
                .collect(Collectors.toMap(OptionChampOperation::getId, o -> o));

        int ordre = 1;
        for (UUID id : ids) {
            OptionChampOperation option = optionMap.get(id);
            option.setOrdre(ordre++);
        }

        optionRepository.saveAll(options);

        return ApiResponse.success(null, "Ordre des options mis à jour avec succès");
    }



    private OptionChampOperationResponse mapToResponse(OptionChampOperation option) {
        return new OptionChampOperationResponse(
                option.getId(),
                option.getLabel(),
                option.getValue(),
                option.getOrdre(),
                option.getOrderIndex(),
                option.getIsDefault(),
                option.getChampOperation().getId()
        );
    }
}
