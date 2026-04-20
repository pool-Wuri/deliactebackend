package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ChampOperationRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OptionChampOperationRequest;
import com.deliacte.dto.response.*;
import com.deliacte.entity.*;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.ChampOperationRepository;
import com.deliacte.repository.EntityObjectRepository;
import com.deliacte.repository.OperationRepository;
import com.deliacte.repository.OptionChampOperationRepository;
import com.deliacte.service.ChampOperationService;
import com.deliacte.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChampOperationServiceImpl implements ChampOperationService {
    
    private final ChampOperationRepository champOperationRepository;
    private final OperationRepository operationRepository;
    private final EntityObjectRepository entityObjectRepository;
    private  final OptionChampOperationRepository optionChampOperationRepository;

    @Override
    public ApiResponse<ChampOperationResponse> create(ChampOperationRequest request) {


        
        ChampOperation champ = ChampOperation.builder()
                .code(request.getCode())
                .label(request.getLabel())
                .placeholder(request.getPlaceholder())
                .helpText(request.getHelpText())
                .fieldType(request.getFieldType())
                .required(request.getRequired() != null ? request.getRequired() : false)
                .minLength(request.getMinLength())
                .maxLength(request.getMaxLength())
                .pattern(request.getPattern())
                .defaultValue(request.getDefaultValue())


                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        
        // Créer les options si c'est un champ de type select, radio, checkbox
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<OptionChampOperation> options = new ArrayList<>();
            for (int i = 0; i < request.getOptions().size(); i++) {
                var optionRequest = request.getOptions().get(i);
                OptionChampOperation option = OptionChampOperation.builder()
                        .value(optionRequest.getValue())
                        .label(optionRequest.getLabel())
                        .orderIndex(i + 1)
                        .champOperation(champ)
                        .build();
                options.add(option);
            }

            champ.setOptions(options);
        }


        if (request.getOperationId() != null) {
            champ.setOperation(
                    operationRepository.findById(request.getOperationId())
                            .orElseThrow(() -> new RuntimeException("Opération introuvable"))
            );
        }

        if (request.getEntityObjectId() != null) {
            champ.setEntityObject(
                    entityObjectRepository.findById(request.getEntityObjectId())
                            .orElseThrow(() -> new RuntimeException("EntityObject introuvable"))
            );
        }
        champ = champOperationRepository.save(champ);
        
        return ApiResponse.success(mapToResponse(champ), "Champ créé avec succès");
    }
    
    @Override
    public ApiResponse<ChampOperationResponse> update(UUID id, ChampOperationRequest request) {
        ChampOperation champ = champOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Champ non trouvé"));
        
        champ.setCode(request.getCode());
        champ.setLabel(request.getLabel());
        champ.setPlaceholder(request.getPlaceholder());
        champ.setHelpText(request.getHelpText());
        champ.setFieldType(request.getFieldType());
        champ.setRequired(request.getRequired() != null ? request.getRequired() : champ.getRequired());
        champ.setMinLength(request.getMinLength());
        champ.setMaxLength(request.getMaxLength());
        champ.setPattern(request.getPattern());
        champ.setDefaultValue(request.getDefaultValue());
        if (request.getActive() != null) {
            champ.setActive(request.getActive());
        }
        
        // Mettre à jour les options
        if (request.getOptions() != null) {
            // Supprimer les anciennes options
            if (champ.getOptions() != null) {
                champ.getOptions().clear();
            } else {
                champ.setOptions(new ArrayList<>());
            }
            
            // Ajouter les nouvelles options
            for (int i = 0; i < request.getOptions().size(); i++) {
                var optionRequest = request.getOptions().get(i);
                OptionChampOperation option = OptionChampOperation.builder()
                        .value(optionRequest.getValue())
                        .label(optionRequest.getLabel())
                        .orderIndex(i + 1)
                        .champOperation(champ)
                        .build();
                champ.getOptions().add(option);
            }
        }
        
        champ = champOperationRepository.save(champ);
        
        return ApiResponse.success(mapToResponse(champ), "Champ mis à jour avec succès");
    }


    @Transactional
    @Override
    public ApiResponse<List<ChampOperationResponse>> associateChampIdsToOperation(
            UUID operationId, IdListRequest champIds) {

        // 1. Récupérer l'opération
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));

        // 2. Récupérer tous les champs existants par leurs IDs
        List<ChampOperation> champs = champOperationRepository.findAllById(champIds.getIds());

        // 3. Associer chaque champ à l'opération et gérer l'ordre
        int order = 1; // commencer à 1, ou récupérer le max existant si besoin
        for (ChampOperation champ : champs) {
            champ.setOperation(operation);
            champ.setOrderIndex(order++);
        }
        // 4. Mettre à jour la liste des champs de l'opération
        operation.getChampOperations().addAll(champs);

        // 5. Sauvegarder l'opération (cascade = ALL persiste les champs)
        operationRepository.save(operation);

        // 6. Mapper et retourner
        List<ChampOperationResponse> responses = champs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses, "Champs associés à l'opération avec succès");
    }


    
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ChampOperationResponse> getById(UUID id) {
        ChampOperation champ = champOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Champ non trouvé"));
        
        return ApiResponse.success(mapToResponse(champ), "Champ récupéré avec succès");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ChampOperationResponse>> getAll(Pageable pageable) {
        Page<ChampOperation> page = champOperationRepository.findAllByDeletedFalse(pageable);
        
        PageResponse<ChampOperationResponse> response = PageResponse.<ChampOperationResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        
        return ApiResponse.success(response, "Liste des champs récupérée avec succès");
    }
    
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ChampOperationResponse>> getByOperationId(UUID operationId) {
        List<ChampOperation> champs = champOperationRepository.findByOperationIdAndDeletedFalseOrderByOrderIndexAsc(operationId);
        
        List<ChampOperationResponse> responses = champs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses, "Champs de l'opération récupérés avec succès");
    }

    @Override
    public ApiResponse<List<ChampOperationResponse>> getByEntityObjectId(UUID entityObjectId) {

        List<ChampOperation> champs =
                champOperationRepository.findByEntityObjectIdAndDeletedFalseOrderByOrderIndexAsc(entityObjectId);

        List<ChampOperationResponse> responses = champs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(
                responses,
                "Champs liés à l'EntityObject récupérés avec succès "+ responses.size()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ChampOperationResponse>> getPublicByOperationId(UUID operationId) {
        List<ChampOperation> champs = champOperationRepository.findByOperationIdAndActiveAndDeletedFalseOrderByOrderIndexAsc(operationId, true);
        
        List<ChampOperationResponse> responses = champs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses, "Champs actifs de l'opération récupérés avec succès");
    }
    
    @Override
    public ApiResponse<Void> delete(UUID id) {
        ChampOperation champ = champOperationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Champ non trouvé"));
        
        champ.setDeleted(true);
        champOperationRepository.save(champ);
        
        return ApiResponse.<Void>success(null, "Champ supprimé avec succès");
    }
    
    @Override
    public ApiResponse<Void> reorder(UUID operationId, List<UUID> champIds) {
        for (int i = 0; i < champIds.size(); i++) {
            ChampOperation champ = champOperationRepository.findByIdAndDeletedFalse(champIds.get(i))
                    .orElseThrow(() -> new ResourceNotFoundException("Champ non trouvé"));
            champ.setOrderIndex(i + 1);
            champOperationRepository.save(champ);
        }
        
        return ApiResponse.<Void>success(null, "Ordre des champs mis à jour avec succès");
    }


    @Override
    @Transactional
    public ApiResponse<Void> linkEntityAndChampOperationToOperation(
            UUID entityId,
            UUID operationId,
            IdListRequest champIds
    ) {

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable"));

        EntityObject entityObject = entityObjectRepository.findById(entityId)
                .orElseThrow(() -> new ResourceNotFoundException("EntityObject introuvable"));

        // 1️⃣ Lier l'entité à l'opération
        operation.getEntityObjects().add(entityObject);

        // 2️⃣ Lier les champs à l’opération via la méthode helper
        int orderIndex = 1;
        for (UUID champId : champIds.getIds()) {

            ChampOperation champ = champOperationRepository
                    .findByIdAndDeletedFalse(champId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Champ non trouvé : " + champId)
                    );

            champ.setOrderIndex(orderIndex++);
            operation.addChampOperation(champ); // ⭐ LA BONNE PRATIQUE
        }

        operationRepository.save(operation);

        return ApiResponse.success(null,
                "EntityObject et champs liés avec succès à l'opération");
    }


    @Override
    public ApiResponse<Void> reorderEntity(UUID entityId, List<UUID> champIds) {
        for (int i = 0; i < champIds.size(); i++) {
            ChampOperation champ = champOperationRepository.findByIdAndDeletedFalse(champIds.get(i))
                    .orElseThrow(() -> new ResourceNotFoundException("Champ non trouvé"));
            champ.setOrder(i + 1);
            champOperationRepository.save(champ);
        }

        return ApiResponse.<Void>success(null, "Ordre des champs mis à jour avec succès");
    }

    @Override
    public ApiResponse<OperationFormResponseDto> getOperationForm(UUID operationId, UUID procedureId) {

        // 1. Si procedureId est fourni, récupérer l'opération racine si operationId est null
        if  (procedureId != null) {
            // Rechercher l'opération de la procédure qui n'a pas de précédent
            Operation rootOperation = operationRepository
                    .findFirstByProcedureIdAndPreviousOperationsIsEmpty(procedureId)
                    .orElseThrow(() -> new ResourceNotFoundException("Aucune opération initiale trouvée pour cette procédure"));
            operationId = rootOperation.getId();
        }
        // 1. Charger les champs
        List<ChampOperation> champsAvecEntite =
                champOperationRepository.findByOperationGroupedByEntity(operationId);

        List<ChampOperation> champsSansEntite =
                champOperationRepository.findGlobalByOperation(operationId);

        // 2. Grouper les champs par EntityObject
        Map<EntityObject, List<ChampOperation>> groupedByEntity =
                champsAvecEntite.stream()
                        .collect(Collectors.groupingBy(ChampOperation::getEntityObject));

        // 3. Construire les DTO Entity + Champs
        List<EntityObjectWithChampOperationDto> entities = new ArrayList<>();

        for (Map.Entry<EntityObject, List<ChampOperation>> entry : groupedByEntity.entrySet()) {

            EntityObject entity = entry.getKey();
            List<ChampOperation> champs = entry.getValue();

            List<ChampOperationResponseDto> champDtos = new ArrayList<>();

            for (ChampOperation champ : champs) {
                champDtos.add(buildChampOperationDto(champ));
            }

            EntityObjectWithChampOperationDto entityDto =
                    EntityObjectWithChampOperationDto.builder()
                            .entityId(entity.getId())
                            .entityCode(entity.getCode())
                            .entityLabel(entity.getName())
                            .champOperations(champDtos)
                            .build();

            entities.add(entityDto);
        }

        // 4. Construire les champs globaux (sans entité)
        List<ChampOperationResponseDto> otherChampOperations = new ArrayList<>();

        for (ChampOperation champ : champsSansEntite) {
            otherChampOperations.add(buildChampOperationDto(champ));
        }
        String operationName = null;
        String procedureName =null;



        for (ChampOperation champ : champsSansEntite) {
            if (champ == null) {
                continue;
            }

            Operation operation = champ.getOperation();
            if (operation == null) {
                continue;
            }

            // opération
            if (operationName == null && operation.getName() != null) {
                operationName = operation.getName();
            }

            // procédure
            Procedure procedure = operation.getProcedure();
            if (procedureName == null && procedure != null && procedure.getName() != null) {
                procedureName = procedure.getName();
                procedureId = procedure.getId();
            }

            // si on a trouvé les deux, on sort
            if (operationName != null && procedureName != null) {
                break;
            }
        }


        String numeroDossier = CodeGenerator.generateNumDossier();

        // Charger les infos de paiement depuis l'opération
        Boolean opHasPayment = null;
        java.math.BigDecimal opFee = null;
        Boolean procHasPayment = null;
        java.math.BigDecimal procFee = null;
        final UUID finalOpId = operationId;
        Operation loadedOp = operationRepository.findById(finalOpId).orElse(null);
        if (loadedOp != null) {
            opHasPayment = loadedOp.getHasPayment();
            opFee = loadedOp.getFee();
            if (loadedOp.getProcedure() != null) {
                procHasPayment = loadedOp.getProcedure().getHasPayment();
                procFee = loadedOp.getProcedure().getFee();
            }
        }

        // 5. Construire la réponse finale
        OperationFormResponseDto responseDto =
                OperationFormResponseDto.builder()
                        .currentOperationId(operationId)
                        .procedureName(procedureName)
                        .procedureId(procedureId)
                        .numeroDossier(numeroDossier)
                        .operationName(operationName)
                        .operationHasPayment(opHasPayment)
                        .operationFee(opFee)
                        .procedureHasPayment(procHasPayment)
                        .procedureFee(procFee)
                        .entities(entities)
                        .otherChampOperations(otherChampOperations)
                        .build();

        // 6. Retour API standardisé
        return ApiResponse.success(
                responseDto,
                "Formulaire de l'opération chargé avec succès"
        );



    }




    @Override
    public ApiResponse<List<ChampOperationResponse>> createAll(
            List<ChampOperationRequest> requests) {

        List<ChampOperationResponse> responses = new ArrayList<>();

        int champOrder = 1;

        for (ChampOperationRequest request : requests) {

            ChampOperation champ = buildChampOperation(request, champOrder++);
            champOperationRepository.save(champ);

            // Enregistrer les options si présentes
            if (request.getOptions() != null && !request.getOptions().isEmpty()) {
                saveOptions(champ, request.getOptions());
            }

            responses.add(mapToResponse(champ));
        }

        return ApiResponse.success(responses, "Champs et options enregistrés avec succès");
    }

    // ===================== PRIVATE METHODS =====================

    private ChampOperation buildChampOperation(
            ChampOperationRequest request,
            int orderIndex) {

        ChampOperation champ = ChampOperation.builder()
                .code(request.getCode())
                .label(request.getLabel())
                .placeholder(request.getPlaceholder())
                .helpText(request.getHelpText())
                .fieldType(request.getFieldType())
                .required(Boolean.TRUE.equals(request.getRequired()))
                .minLength(request.getMinLength())
                .maxLength(request.getMaxLength())
                .pattern(request.getPattern())
                .defaultValue(request.getDefaultValue())
                .orderIndex(
                        request.getOrderIndex() != null
                                ? request.getOrderIndex()
                                : orderIndex
                )
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();

        if (request.getOperationId() != null) {
            champ.setOperation(
                    operationRepository.findById(request.getOperationId())
                            .orElseThrow(() -> new RuntimeException("Opération introuvable"))
            );
        }

        if (request.getEntityObjectId() != null) {
            champ.setEntityObject(
                    entityObjectRepository.findById(request.getEntityObjectId())
                            .orElseThrow(() -> new RuntimeException("EntityObject introuvable"))
            );
        }

        return champ;
    }

    private void saveOptions(
            ChampOperation champ,
            List<OptionChampOperationRequest> options) {

        Boolean defaultAlreadySet = false;
        int ordre = 1;

        for (OptionChampOperationRequest optReq : options) {

            Boolean isDefault = Boolean.TRUE.equals(optReq.getIsDefault());

            // Une seule valeur par défaut
            if (isDefault && defaultAlreadySet) {
                isDefault = false;
            }

            if (isDefault) {
                defaultAlreadySet = true;
            }

            OptionChampOperation option = OptionChampOperation.builder()
                    .label(optReq.getLabel())
                    .value(
                            optReq.getValue() != null
                                    ? optReq.getValue()
                                    : optReq.getLabel()
                    )
                    .ordre(
                            optReq.getOrdre() != null
                                    ? optReq.getOrdre()
                                    : ordre
                    )
                    .orderIndex(
                            optReq.getOrderIndex() != null
                                    ? optReq.getOrderIndex()
                                    : ordre
                    )
                    .isDefault(isDefault)
                    .champOperation(champ)
                    .build();

            optionChampOperationRepository.save(option);
            ordre++;
        }
    }




    private ChampOperationResponseDto buildChampOperationDto(ChampOperation champ) {

        List<OptionChampOperationResponse> optionDtos = new ArrayList<>();

        if (champ.getOptions() != null) {
            for (OptionChampOperation option : champ.getOptions()) {
                optionDtos.add(
                        OptionChampOperationResponse.builder()
                                .id(option.getId())
//                                .code(option.getCode())
                                .label(option.getLabel())
//                                .order(option.getOrder())
                                .build()
                );
            }
        }

        return ChampOperationResponseDto.builder()
                .id(champ.getId())
                .code(champ.getCode())
                .label(champ.getLabel())
                .placeholder(champ.getPlaceholder())
                .helpText(champ.getHelpText())
                .fieldType(champ.getFieldType())
                .required(champ.getRequired())
                .minLength(champ.getMinLength())
                .maxLength(champ.getMaxLength())
                .pattern(champ.getPattern())
                .defaultValue(champ.getDefaultValue())
                .order(champ.getOrder())
                .orderIndex(champ.getOrderIndex())
                .active(champ.getActive())
                .options(optionDtos)
                .build();
    }




    private ChampOperationResponse mapToResponse(ChampOperation champ) {
        ChampOperationResponse.ChampOperationResponseBuilder builder = ChampOperationResponse.builder()
                .id(champ.getId())
                .code(champ.getCode())
                .label(champ.getLabel())
                .placeholder(champ.getPlaceholder())
                .helpText(champ.getHelpText())
                .fieldType(champ.getFieldType())
                .required(champ.getRequired())
                .minLength(champ.getMinLength())
                .maxLength(champ.getMaxLength())
                .pattern(champ.getPattern())
                .defaultValue(champ.getDefaultValue())
                .orderIndex(champ.getOrderIndex())
                .active(champ.getActive())
                .createdAt(champ.getCreatedAt())
                .updatedAt(champ.getUpdatedAt());
        
        if (champ.getOperation() != null) {
            builder.operation(OperationSimpleResponse.builder()
                    .id(champ.getOperation().getId())
                    .name(champ.getOperation().getName())
                    .build());
        }
        
        if (champ.getOptions() != null && !champ.getOptions().isEmpty()) {
            List<OptionChampOperationResponse> optionResponses = champ.getOptions().stream()
                    .map(opt -> OptionChampOperationResponse.builder()
                            .id(opt.getId())
                            .value(opt.getValue())
                            .label(opt.getLabel())
                            .orderIndex(opt.getOrderIndex())
                            .build())
                    .collect(Collectors.toList());
            builder.options(optionResponses);
        }
        
        return builder.build();
    }
}
