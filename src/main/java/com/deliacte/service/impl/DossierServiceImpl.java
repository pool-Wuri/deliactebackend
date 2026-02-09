package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.DossierRequest;
import com.deliacte.dto.response.*;
import com.deliacte.entity.*;
import com.deliacte.enums.DossierOperationStatus;
import com.deliacte.enums.DossierStatus;
import com.deliacte.enums.DossierActionType;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.*;
import com.deliacte.service.DossierChampValueService;
import com.deliacte.service.DossierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierServiceImpl implements DossierService {

    private final DossierRepository dossierRepository;
    private final OperationRepository operationRepository;
    private final DossierOperationRepository dossierOperationRepository;
    private final DossierHistoriqueRepository dossierHistoriqueRepository;
    private final UserRepository userRepository;
    private final DossierChampValueService dossierChampValueService; // Injection du service
    private  final  ChampOperationRepository champOperationRepository;

    @Override
    @Transactional
    public Dossier processDossierSubmission(DossierRequest request, UUID userId) {
        log.info("Traitement de la soumission pour l'utilisateur: {}", userId);
        User currentUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Dossier dossier = findOrCreateDossier(request, currentUser);

        DossierOperation currentStep = dossierOperationRepository.findByDossierIdAndOperationIdAndStatus(
                        dossier.getId(), request.getCurrentOperationId(), DossierOperationStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("Aucune étape en attente trouvée pour ce dossier à cette opération."));

       List <Operation> targetOperation = determineTargetOperations(request, currentStep.getOperation());

        DossierHistorique historique = createDossierHistory(request, dossier, currentUser, currentStep.getOperation(), targetOperation);

        if (request.getChampValues() != null && !request.getChampValues().isEmpty()) {
            for (var champValueRequest : request.getChampValues()) {
                // Appel correct au service de versioning
                dossierChampValueService.addOrUpdateChampValue(champValueRequest, historique);
            }
        }

        updateCurrentStep(currentStep, request, currentUser);

        if (!targetOperation.isEmpty()) {
            createNewSteps(dossier, targetOperation);
        }

        updateDossierGlobalStatus(dossier, targetOperation, currentStep.getOperation());

        return dossierRepository.save(dossier);
    }






    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<DossierListResponse>> getMyDossiers(UUID userId) {

        List<Dossier> dossiers = dossierRepository.findAllByUserId(userId);

        List<DossierListResponse> response = dossiers.stream()
                .map(dossier -> {

                    DossierOperation currentOp =
                            dossierOperationRepository
                                    .findFirstByDossierIdAndStatusOrderByReceivedAtDesc(
                                            dossier.getId(),
                                            DossierOperationStatus.PENDING
                                    )
                                    .orElse(null);

                    return DossierListResponse.builder()
                            .id(dossier.getId())
                            .dossierNumber(dossier.getDossierNumber())
                            .procedureId(dossier.getProcedure().getId())
                            .procedureName(dossier.getProcedure().getName())
                            .citoyenName(dossier.getUser().getFirstName() + " "+ dossier.getUser().getLastName())
                            .status(dossier.getStatus())
                            .submittedAt(dossier.getSubmittedAt())
                            .currentOperationId(
                                    currentOp != null ? currentOp.getOperation().getId() : null
                            )
                            .currentOperationName(
                                    currentOp != null ? currentOp.getOperation().getName() : null
                            )
                            .build();
                })
                .toList();

        return ApiResponse.success(
                response,
                "Liste des dossiers récupérée avec succès"
        );
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<DossierListResponse>> getDossiersByOperation(UUID operationId) {

        List<Dossier> dossiers = dossierRepository.findAllByOperationId(
                operationId,
                DossierOperationStatus.PENDING
        );

        List<DossierListResponse> response = dossiers.stream()
                .map(dossier -> {

                    DossierOperation currentOp =
                            dossier.getOperationSteps().stream()
                                    .filter(op ->
                                            op.getOperation().getId().equals(operationId)
                                                    && op.getStatus() == DossierOperationStatus.PENDING
                                    )
                                    .findFirst()
                                    .orElse(null);

                    return DossierListResponse.builder()
                            .id(dossier.getId())
                            .dossierNumber(dossier.getDossierNumber())
                            .procedureId(dossier.getProcedure().getId())
                            .procedureName(dossier.getProcedure().getName())
                            .citoyenName(
                                    (dossier.getCreatedBy() != null)
                                            ? String.format("%s %s",
                                            Optional.ofNullable(dossier.getCreatedBy().getFirstName()).orElse(""),
                                            Optional.ofNullable(dossier.getCreatedBy().getLastName()).orElse("")
                                    ).trim()
                                            : ""
                            )
                            .status(dossier.getStatus())
                            .submittedAt(dossier.getSubmittedAt())
                            .currentOperationId(
                                    currentOp != null ? currentOp.getOperation().getId() : null
                            )
                            .currentOperationName(
                                    currentOp != null ? currentOp.getOperation().getName() : null
                            )
                            .build();
                })
                .toList();

        return ApiResponse.success(
                response,
                "Liste des dossiers pour l’opération récupérée avec succès"
        );
    }





    @Override
@Transactional(readOnly = true)
public ApiResponse<OperationFormResponseDto> getOperationFormByNumeroDossier(String numeroDossier) {

    if (numeroDossier == null || numeroDossier.isEmpty()) {
        throw new IllegalArgumentException("Le numéro de dossier est obligatoire");
    }

    // 1. Récupérer le dossier avec la procédure
    Dossier dossier = dossierRepository.findByDossierNumberWithProcedure(numeroDossier)
            .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + numeroDossier));

    UUID procedureId = dossier.getProcedure() != null ? dossier.getProcedure().getId() : null;
    String procedureName = dossier.getProcedure() != null ? dossier.getProcedure().getName() : null;

    // 2. Récupérer toutes les valeurs du dossier depuis l'historique
    Map<UUID, String> champValuesMap = new HashMap<>();
    for (DossierHistorique historique : dossier.getHistoriques()) {
        for (DossierChampValue val : historique.getChampValues()) {
            // La dernière valeur écrase l'ancienne si plusieurs versions existent
            champValuesMap.put(val.getChampOperation().getId(), val.getValue());
        }
    }

    // 3. Récupérer toutes les opérations associées aux champs (optionnel, pour grouper par EntityObject)
    List<ChampOperation> champsAvecEntite = champOperationRepository.findByProcedureGroupedByEntity(procedureId);
    List<ChampOperation> champsSansEntite = champOperationRepository.findGlobalByProcedure(procedureId);

    // 4. Grouper les champs par EntityObject
    Map<EntityObject, List<ChampOperation>> groupedByEntity =
            champsAvecEntite.stream().collect(Collectors.groupingBy(ChampOperation::getEntityObject));

    List<EntityObjectWithChampOperationDto> entities = new ArrayList<>();
    for (Map.Entry<EntityObject, List<ChampOperation>> entry : groupedByEntity.entrySet()) {
        EntityObject entity = entry.getKey();
        List<ChampOperationResponseDto> champDtos = entry.getValue().stream().map(champ -> {
            ChampOperationResponseDto dto = buildChampOperationDto(champ);
            if (champValuesMap.containsKey(champ.getId())) {
                dto.setDefaultValue(champValuesMap.get(champ.getId()));
            }
            return dto;
        }).toList();

        entities.add(EntityObjectWithChampOperationDto.builder()
                .entityId(entity.getId())
                .entityCode(entity.getCode())
                .entityLabel(entity.getName())
                .champOperations(champDtos)
                .build());
    }

    // 5. Champs globaux (sans entité)
    List<ChampOperationResponseDto> otherChampOperations = champsSansEntite.stream().map(champ -> {
        ChampOperationResponseDto dto = buildChampOperationDto(champ);
        if (champValuesMap.containsKey(champ.getId())) {
            dto.setDefaultValue(champValuesMap.get(champ.getId()));
        }
        return dto;
    }).toList();

    // 6. Aucun currentOperationId puisque tu veux retourner null
    UUID currentOperationId = null;
    String operationName = null;

    // 7. Construire la réponse finale
    OperationFormResponseDto responseDto = OperationFormResponseDto.builder()
            .currentOperationId(currentOperationId)
            .procedureId(procedureId)
            .procedureName(procedureName)
            .numeroDossier(numeroDossier)
            .operationName(operationName)
            .entities(entities)
            .otherChampOperations(otherChampOperations)
            .build();

    return ApiResponse.success(responseDto, "Formulaire du dossier chargé avec succès, incluant toutes les valeurs historiques");
}





    // ... (les autres méthodes restent les mêmes)

    private Dossier findOrCreateDossier(DossierRequest request, User currentUser) {
        String numeroDossier = request.getNumeroDossier();
        if (numeroDossier != null && !numeroDossier.trim().isEmpty()) {
            return dossierRepository.findByDossierNumber(numeroDossier)
                    .orElseGet(() -> createNewDossier(numeroDossier, request.getCurrentOperationId(), currentUser));
        }
        return createNewDossier(generateNumeroDossier(), request.getCurrentOperationId(), currentUser);
    }

    private Dossier createNewDossier(String numeroDossier, UUID firstOperationId, User currentUser) {
        Operation firstOperation = operationRepository.findById(firstOperationId)
//                .filter(Operation::getIsFirstOperation)
                .orElseThrow(() -> new RuntimeException("L'opération de départ n'est pas une première opération valide."));

        Dossier newDossier = Dossier.builder()
                .dossierNumber(numeroDossier)
                .procedure(firstOperation.getProcedure())
                .user(currentUser)
                .status(DossierStatus.IN_PROGRESS)
                .submittedAt(LocalDateTime.now())
                .build();

        DossierOperation initialStep = DossierOperation.builder()
                .dossier(newDossier)
                .operation(firstOperation)
                .status(DossierOperationStatus.PENDING)
                .build();
        newDossier.addOperationStep(initialStep);

        return dossierRepository.save(newDossier);
    }

//    private Operation determineTargetOperation(DossierRequest request, Operation fromOperation) {
//        if (request.getSendToNext()) {
//            return fromOperation.getNextOperations().stream().findFirst().orElse(null);
//        } else {
//            UUID previousOpId = request.getPreviousOperationId();
//            if (previousOpId == null) throw new RuntimeException("Opération précédente requise pour un rejet.");
//            return fromOperation.getPreviousOperations().stream()
//                    .filter(op -> op.getId().equals(previousOpId))
//                    .findFirst()
//                    .orElseThrow(() -> new RuntimeException("Opération précédente invalide."));
//        }
//    }


    private List<Operation> determineTargetOperations(
            DossierRequest request,
            Operation fromOperation
    ) {

        // Cas 1 : envoi vers les opérations suivantes (branching possible)
        if (Boolean.TRUE.equals(request.getSendToNext())) {

            Set<Operation> nextOperations = fromOperation.getNextOperations();
//
//            if (nextOperations == null || nextOperations.isEmpty()) {
//                throw new RuntimeException("Aucune opération suivante définie.");
//            }

            return new ArrayList<>(nextOperations);
        }

        // Cas 2 : retour vers une ou plusieurs opérations précédentes
        List<UUID> previousOpIds = request.getPreviousOperationId();

        if (previousOpIds == null || previousOpIds.isEmpty()) {
            throw new RuntimeException("Au moins une opération précédente est requise pour un rejet.");
        }

        List<Operation> previousOperations = fromOperation.getPreviousOperations().stream()
                .filter(op -> previousOpIds.contains(op.getId()))
                .toList();

        if (previousOperations.isEmpty()) {
            throw new RuntimeException("Aucune opération précédente valide trouvée.");
        }

        return previousOperations;
    }


    private DossierHistorique createDossierHistory(DossierRequest request, Dossier dossier, User user, Operation from, List<Operation> target) {
        DossierStatus newStatus = determineNewStatus(from, target);

        DossierHistorique historique = DossierHistorique.builder()
                .dossier(dossier)
                .user(user)
                .fromOperation(from)
                .targetOperations(target)
                .comment(request.getCommentaire())
                .actionType(request.getSendToNext() ? DossierActionType.SUBMIT : DossierActionType.REJECT)
                .status(newStatus)
                .build();

        return dossierHistoriqueRepository.save(historique);
    }

    private void updateCurrentStep(DossierOperation currentStep, DossierRequest request, User currentUser) {
        currentStep.setStatus(request.getSendToNext() ? DossierOperationStatus.COMPLETED : DossierOperationStatus.REJECTED);
        currentStep.setCompletedAt(LocalDateTime.now());
        currentStep.setProcessedBy(currentUser);
        currentStep.setComment(request.getCommentaire());
        dossierOperationRepository.save(currentStep);
    }

//    private void createNewStep(Dossier dossier, Operation targetOperation) {
//        DossierOperation newStep = DossierOperation.builder()
//                .dossier(dossier)
//                .operation(targetOperation)
//                .status(DossierOperationStatus.PENDING)
//                .build();
//        dossier.addOperationStep(newStep);
//    }


    private void createNewSteps(Dossier dossier, List<Operation> targetOperations) {
        if (targetOperations == null || targetOperations.isEmpty()) {
            return;
        }

        for (Operation target : targetOperations) {
            DossierOperation newStep = DossierOperation.builder()
                    .dossier(dossier)
                    .operation(target)
                    .status(DossierOperationStatus.PENDING)
                    .build();
            dossier.addOperationStep(newStep);
        }
    }


    private void updateDossierGlobalStatus(
            Dossier dossier,
            List<Operation> targetOperations,
            Operation fromOperation
    ) {
        if (targetOperations == null || targetOperations.isEmpty()) {
            if (Boolean.TRUE.equals(fromOperation.getIsLastOperation())) {
                dossier.setStatus(DossierStatus.COMPLETED);
                dossier.setCompletedAt(LocalDateTime.now());
            } else {
                dossier.setStatus(DossierStatus.REJECTED);
            }
        } else {
            dossier.setStatus(DossierStatus.IN_PROGRESS);
        }
    }

//
//    private void updateDossierGlobalStatus(Dossier dossier, Operation targetOperation, Operation fromOperation) {
//        if (targetOperation == null) {
//            if (fromOperation.getIsLastOperation()) {
//                dossier.setStatus(DossierStatus.COMPLETED);
//                dossier.setCompletedAt(LocalDateTime.now());
//            } else {
//                dossier.setStatus(DossierStatus.REJECTED);
//            }
//        } else {
//            dossier.setStatus(DossierStatus.IN_PROGRESS);
//        }
//    }

//    private DossierStatus determineNewStatus(Operation from, Operation target) {
//        if (target == null) {
//            if (from.getIsLastOperation()) {
//                return DossierStatus.COMPLETED;
//            } else {
//                return DossierStatus.REJECTED;
//            }
//        } else {
//            return DossierStatus.IN_PROGRESS;
//        }
//    }


    private DossierStatus determineNewStatus(
            Operation from,
            List<Operation> targets
    ) {
        if (targets == null || targets.isEmpty()) {
            return Boolean.TRUE.equals(from.getIsLastOperation())
                    ? DossierStatus.COMPLETED
                    : DossierStatus.REJECTED;
        }
        return DossierStatus.IN_PROGRESS;
    }


    private String generateNumeroDossier() {
        return "DOS-" + System.currentTimeMillis();
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







}
