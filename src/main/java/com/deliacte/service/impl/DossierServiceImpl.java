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
    private final DossierChampValueService dossierChampValueService;
    private final ChampOperationRepository champOperationRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // SOUMISSION PRINCIPALE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Dossier processDossierSubmission(DossierRequest request, UUID userId) {
        log.info("Traitement soumission – utilisateur: {}, opération: {}",
                userId, request.getCurrentOperationId());

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + userId));

        // 1. Trouver ou créer le dossier
        Dossier dossier = findOrCreateDossier(request, currentUser);

        // 2. Trouver l'étape courante (doit être PENDING)
        DossierOperation currentStep = dossierOperationRepository
                .findByDossierIdAndOperationIdAndStatus(
                        dossier.getId(),
                        request.getCurrentOperationId(),
                        DossierOperationStatus.PENDING)
                .orElseThrow(() -> new RuntimeException(
                        "Aucune étape PENDING trouvée pour ce dossier à l'opération "
                                + request.getCurrentOperationId()));

        boolean isApproval = Boolean.TRUE.equals(request.getSendToNext());

        // 3. Déterminer les opérations cibles
        List<Operation> targetOperations = determineTargetOperations(request, currentStep.getOperation());

        // 4. Enregistrer l'historique
        DossierHistorique historique = createDossierHistory(
                request, dossier, currentUser,
                currentStep.getOperation(), targetOperations, isApproval);

        // 5. Sauvegarder les valeurs des champs
        if (request.getChampValues() != null && !request.getChampValues().isEmpty()) {
            for (var champValueRequest : request.getChampValues()) {
                dossierChampValueService.addOrUpdateChampValue(champValueRequest, historique);
            }
        }

        // 6. Marquer l'étape courante comme COMPLETED ou REJECTED
        updateCurrentStep(currentStep, isApproval, currentUser, request.getCommentaire());

        // 7. En cas de REJET : annuler toutes les étapes PENDING des autres opérations
        //    pour que les agents en aval ne voient plus ce dossier
        if (!isApproval) {
            cancelOtherPendingSteps(dossier, currentStep);
        }

        // 8. Créer les nouvelles étapes sans créer de doublons PENDING
        if (!targetOperations.isEmpty()) {
            createNewStepsNoDuplicate(dossier, targetOperations);
        }

        // 9. Mettre à jour le statut global du dossier
        updateDossierGlobalStatus(dossier, targetOperations, currentStep.getOperation(), isApproval);

        Dossier saved = dossierRepository.save(dossier);
        log.info("Dossier {} traité avec succès – nouveau statut: {}",
                saved.getDossierNumber(), saved.getStatus());
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LECTURE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<DossierListResponse>> getMyDossiers(UUID userId) {
        List<Dossier> dossiers = dossierRepository.findAllByUserId(userId);

        List<DossierListResponse> response = dossiers.stream()
                .map(dossier -> {
                    DossierOperation currentOp =
                            dossierOperationRepository
                                    .findFirstByDossierIdAndStatusOrderByReceivedAtDesc(
                                            dossier.getId(), DossierOperationStatus.PENDING)
                                    .orElse(null);

                    return DossierListResponse.builder()
                            .id(dossier.getId())
                            .dossierNumber(dossier.getDossierNumber())
                            .procedureId(dossier.getProcedure().getId())
                            .procedureName(dossier.getProcedure().getName())
                            .citoyenName(dossier.getUser().getFirstName() + " " + dossier.getUser().getLastName())
                            .status(dossier.getStatus())
                            .submittedAt(dossier.getSubmittedAt())
                            .currentOperationId(currentOp != null ? currentOp.getOperation().getId() : null)
                            .currentOperationName(currentOp != null ? currentOp.getOperation().getName() : null)
                            .build();
                })
                .toList();

        return ApiResponse.success(response, "Liste des dossiers récupérée avec succès");
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<DossierListResponse>> getDossiersByOperation(UUID operationId) {
        // Ne retourne que les dossiers ayant une étape PENDING à cette opération
        List<Dossier> dossiers = dossierRepository.findAllByOperationId(
                operationId, DossierOperationStatus.PENDING);

        List<DossierListResponse> response = dossiers.stream()
                .map(dossier -> {
                    DossierOperation currentOp = dossier.getOperationSteps().stream()
                            .filter(op -> op.getOperation().getId().equals(operationId)
                                    && op.getStatus() == DossierOperationStatus.PENDING)
                            .findFirst()
                            .orElse(null);

                    return DossierListResponse.builder()
                            .id(dossier.getId())
                            .dossierNumber(dossier.getDossierNumber())
                            .procedureId(dossier.getProcedure().getId())
                            .procedureName(dossier.getProcedure().getName())
                            .citoyenName(buildCitoyenName(dossier))
                            .status(dossier.getStatus())
                            .submittedAt(dossier.getSubmittedAt())
                            .currentOperationId(currentOp != null ? currentOp.getOperation().getId() : null)
                            .currentOperationName(currentOp != null ? currentOp.getOperation().getName() : null)
                            .build();
                })
                .toList();

        return ApiResponse.success(response, "Liste des dossiers pour l'opération récupérée avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OperationFormResponseDto> getOperationFormByNumeroDossier(String numeroDossier) {
        if (numeroDossier == null || numeroDossier.isEmpty()) {
            throw new IllegalArgumentException("Le numéro de dossier est obligatoire");
        }

        Dossier dossier = dossierRepository.findByDossierNumberWithProcedure(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + numeroDossier));

        UUID procedureId = dossier.getProcedure() != null ? dossier.getProcedure().getId() : null;
        String procedureName = dossier.getProcedure() != null ? dossier.getProcedure().getName() : null;

        // Construire la map des valeurs les plus récentes (actives) par champ.
        // Pour les champs fichier, la valeur utile est filePath (value est null).
        Map<UUID, String> champValuesMap = new HashMap<>();
        for (DossierHistorique historique : dossier.getHistoriques()) {
            for (DossierChampValue val : historique.getChampValues()) {
                if (Boolean.TRUE.equals(val.getIsActive())) {
                    String effectiveValue = (val.getValue() != null && !val.getValue().isBlank())
                            ? val.getValue()
                            : val.getFilePath();
                    if (effectiveValue != null) {
                        champValuesMap.put(val.getChampOperation().getId(), effectiveValue);
                    }
                }
            }
        }

        List<ChampOperation> champsAvecEntite = champOperationRepository.findByProcedureGroupedByEntity(procedureId);
        List<ChampOperation> champsSansEntite = champOperationRepository.findGlobalByProcedure(procedureId);

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

        List<ChampOperationResponseDto> otherChampOperations = champsSansEntite.stream().map(champ -> {
            ChampOperationResponseDto dto = buildChampOperationDto(champ);
            if (champValuesMap.containsKey(champ.getId())) {
                dto.setDefaultValue(champValuesMap.get(champ.getId()));
            }
            return dto;
        }).toList();

        OperationFormResponseDto responseDto = OperationFormResponseDto.builder()
                .currentOperationId(null)
                .procedureId(procedureId)
                .procedureName(procedureName)
                .numeroDossier(numeroDossier)
                .operationName(null)
                .entities(entities)
                .otherChampOperations(otherChampOperations)
                .build();

        return ApiResponse.success(responseDto,
                "Formulaire du dossier chargé avec succès");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIMELINE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<DossierTimelineResponse> getDossierTimeline(String dossierNumber) {
        if (dossierNumber == null || dossierNumber.isBlank()) {
            throw new IllegalArgumentException("Numéro de dossier obligatoire");
        }

        Dossier dossier = dossierRepository.findByDossierNumberWithProcedure(dossierNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + dossierNumber));

        Procedure procedure = dossier.getProcedure();

        // Toutes les opérations de la procédure, dans l'ordre
        List<Operation> allOps = operationRepository
                .findByProcedureIdAndDeletedFalseOrderByOrderIndexAsc(procedure.getId());

        // Toutes les étapes du dossier, indexées par operationId
        List<DossierOperation> dossierSteps =
                dossierOperationRepository.findByDossierIdOrderByReceivedAtAsc(dossier.getId());

        Map<UUID, DossierOperation> stepsByOpId = dossierSteps.stream()
                .collect(Collectors.toMap(
                        ds -> ds.getOperation().getId(),
                        ds -> ds,
                        (a, b) -> b  // si doublon, prendre le dernier
                ));

        // Déterminer quelle étape est "courante" (PENDING la plus récente)
        UUID currentOpId = dossierSteps.stream()
                .filter(ds -> ds.getStatus() == DossierOperationStatus.PENDING)
                .max(Comparator.comparing(DossierOperation::getReceivedAt))
                .map(ds -> ds.getOperation().getId())
                .orElse(null);

        // Construire les steps dans l'ordre de la procédure
        List<DossierOperationStepResponse> steps = new ArrayList<>();
        for (Operation op : allOps) {
            DossierOperation ds = stepsByOpId.get(op.getId());

            String processedByName = null;
            if (ds != null && ds.getProcessedBy() != null) {
                User p = ds.getProcessedBy();
                processedByName = ((p.getFirstName() != null ? p.getFirstName() : "") + " "
                        + (p.getLastName() != null ? p.getLastName() : "")).trim();
                if (processedByName.isBlank()) processedByName = p.getEmail();
            }

            String typeLibelle = (op.getTypeOperation() != null)
                    ? op.getTypeOperation().getLibelle() : null;

            steps.add(DossierOperationStepResponse.builder()
                    .operationId(op.getId())
                    .operationName(op.getName())
                    .orderIndex(op.getOrderIndex())
                    .isCitizenOperation(Boolean.TRUE.equals(op.getIsCitizenOperation()))
                    .typeOperationLibelle(typeLibelle)
                    .stepStatus(ds != null ? ds.getStatus() : null)
                    .processedByName(processedByName)
                    .comment(ds != null ? ds.getComment() : null)
                    .receivedAt(ds != null ? ds.getReceivedAt() : null)
                    .completedAt(ds != null ? ds.getCompletedAt() : null)
                    .isCurrent(op.getId().equals(currentOpId))
                    .isFirst(Boolean.TRUE.equals(op.getIsFirstOperation()))
                    .isLast(Boolean.TRUE.equals(op.getIsLastOperation()))
                    .build());
        }

        String orgName = (procedure.getOrganisation() != null)
                ? procedure.getOrganisation().getName() : null;

        DossierTimelineResponse response = DossierTimelineResponse.builder()
                .dossierNumber(dossier.getDossierNumber())
                .procedureName(procedure.getName())
                .organisationName(orgName)
                .dossierStatus(dossier.getStatus())
                .submittedAt(dossier.getSubmittedAt())
                .completedAt(dossier.getCompletedAt())
                .steps(steps)
                .build();

        return ApiResponse.success(response, "Timeline récupérée avec succès");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTHODES PRIVÉES
    // ─────────────────────────────────────────────────────────────────────────

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
                .orElseThrow(() -> new RuntimeException("Opération introuvable : " + firstOperationId));

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

    /**
     * Détermine les opérations cibles selon l'action :
     * - Approbation : opérations suivantes de l'opération courante
     * - Rejet       : opérations précédentes choisies dans la requête
     */
    private List<Operation> determineTargetOperations(DossierRequest request, Operation fromOperation) {
        if (Boolean.TRUE.equals(request.getSendToNext())) {
            // Approbation → opérations suivantes (branching possible)
            Set<Operation> nextOps = fromOperation.getNextOperations();
            return nextOps != null ? new ArrayList<>(nextOps) : Collections.emptyList();
        }

        // Rejet → opérations précédentes sélectionnées
        List<UUID> previousOpIds = request.getPreviousOperationId();
        if (previousOpIds == null || previousOpIds.isEmpty()) {
            throw new RuntimeException(
                    "Au moins une opération précédente doit être sélectionnée pour un rejet.");
        }

        Set<Operation> previousOps = fromOperation.getPreviousOperations();
        List<Operation> targets = previousOps != null
                ? previousOps.stream()
                        .filter(op -> previousOpIds.contains(op.getId()))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        if (targets.isEmpty()) {
            throw new RuntimeException(
                    "Les opérations précédentes sélectionnées sont invalides pour cette opération.");
        }

        return targets;
    }

    /**
     * Crée l'entrée d'historique pour cette action.
     */
    private DossierHistorique createDossierHistory(DossierRequest request, Dossier dossier,
            User user, Operation from, List<Operation> targets, boolean isApproval) {

        DossierStatus histStatus = determineNewStatus(from, targets, isApproval);

        DossierHistorique historique = DossierHistorique.builder()
                .dossier(dossier)
                .user(user)
                .fromOperation(from)
                .targetOperations(targets)
                .comment(request.getCommentaire())
                .actionType(isApproval ? DossierActionType.SUBMIT : DossierActionType.REJECT)
                .status(histStatus)
                .build();

        return dossierHistoriqueRepository.save(historique);
    }

    /**
     * Marque l'étape courante COMPLETED (approbation) ou REJECTED (rejet).
     */
    private void updateCurrentStep(DossierOperation currentStep, boolean isApproval,
            User processedBy, String comment) {
        currentStep.setStatus(isApproval ? DossierOperationStatus.COMPLETED : DossierOperationStatus.REJECTED);
        currentStep.setCompletedAt(LocalDateTime.now());
        currentStep.setProcessedBy(processedBy);
        currentStep.setComment(comment);
        dossierOperationRepository.save(currentStep);
    }

    /**
     * Lors d'un rejet : passe toutes les étapes PENDING du même dossier
     * (autres que l'étape en cours de traitement) au statut SKIPPED.
     *
     * Cela garantit que les agents des opérations en aval ne voient plus
     * ce dossier dans leur file d'attente.
     */
    private void cancelOtherPendingSteps(Dossier dossier, DossierOperation processedStep) {
        List<DossierOperation> allSteps = dossierOperationRepository
                .findByDossierIdOrderByReceivedAtAsc(dossier.getId());

        List<DossierOperation> toCancel = allSteps.stream()
                .filter(step ->
                        step.getStatus() == DossierOperationStatus.PENDING
                        && !step.getId().equals(processedStep.getId()))
                .collect(Collectors.toList());

        if (toCancel.isEmpty()) return;

        for (DossierOperation step : toCancel) {
            step.setStatus(DossierOperationStatus.SKIPPED);
            step.setCompletedAt(LocalDateTime.now());
            log.info("Étape PENDING annulée (SKIPPED) à l'opération {} suite au rejet à {}",
                    step.getOperation().getId(), processedStep.getOperation().getId());
        }

        dossierOperationRepository.saveAll(toCancel);
    }

    /**
     * Crée une nouvelle étape PENDING pour chaque opération cible,
     * en évitant les doublons : si une étape PENDING existe déjà
     * pour cette opération, on ne la recrée pas.
     */
    private void createNewStepsNoDuplicate(Dossier dossier, List<Operation> targetOperations) {
        if (targetOperations == null || targetOperations.isEmpty()) return;

        // IDs des opérations ayant déjà une étape PENDING active
        Set<UUID> alreadyPending = dossier.getOperationSteps().stream()
                .filter(step -> step.getStatus() == DossierOperationStatus.PENDING)
                .map(step -> step.getOperation().getId())
                .collect(Collectors.toSet());

        for (Operation target : targetOperations) {
            if (alreadyPending.contains(target.getId())) {
                log.info("Étape PENDING déjà existante pour l'opération {} – doublon ignoré.",
                        target.getId());
                continue;
            }

            DossierOperation newStep = DossierOperation.builder()
                    .dossier(dossier)
                    .operation(target)
                    .status(DossierOperationStatus.PENDING)
                    .build();
            dossier.addOperationStep(newStep);
            log.info("Nouvelle étape PENDING créée pour l'opération {}.", target.getId());
        }
    }

    /**
     * Met à jour le statut global du dossier :
     * - Approbation + dernière opération + pas de suivants → COMPLETED
     * - Approbation avec opérations suivantes               → IN_PROGRESS
     * - Rejet avec opérations précédentes                   → IN_PROGRESS (retour en arrière)
     * - Rejet sans opérations précédentes (premier niveau)  → REJECTED
     */
    private void updateDossierGlobalStatus(Dossier dossier, List<Operation> targetOperations,
            Operation fromOperation, boolean isApproval) {

        if (targetOperations == null || targetOperations.isEmpty()) {
            if (isApproval && Boolean.TRUE.equals(fromOperation.getIsLastOperation())) {
                dossier.setStatus(DossierStatus.COMPLETED);
                dossier.setCompletedAt(LocalDateTime.now());
                log.info("Dossier {} COMPLETED – dernière opération approuvée.", dossier.getDossierNumber());
            } else {
                // Rejet sans opération précédente possible → rejet définitif
                dossier.setStatus(DossierStatus.REJECTED);
                log.info("Dossier {} REJECTED définitivement.", dossier.getDossierNumber());
            }
        } else {
            // Des cibles existent → le dossier continue son traitement
            dossier.setStatus(DossierStatus.IN_PROGRESS);
        }
    }

    /**
     * Détermine le statut à enregistrer dans l'historique de cette action.
     */
    private DossierStatus determineNewStatus(Operation from, List<Operation> targets, boolean isApproval) {
        if (targets == null || targets.isEmpty()) {
            if (isApproval && Boolean.TRUE.equals(from.getIsLastOperation())) {
                return DossierStatus.COMPLETED;
            }
            return DossierStatus.REJECTED;
        }
        return DossierStatus.IN_PROGRESS;
    }

    private String generateNumeroDossier() {
        return "DOS-" + System.currentTimeMillis();
    }

    private String buildCitoyenName(Dossier dossier) {
        if (dossier.getCreatedBy() != null) {
            return String.format("%s %s",
                    Optional.ofNullable(dossier.getCreatedBy().getFirstName()).orElse(""),
                    Optional.ofNullable(dossier.getCreatedBy().getLastName()).orElse("")).trim();
        }
        if (dossier.getUser() != null) {
            return String.format("%s %s",
                    Optional.ofNullable(dossier.getUser().getFirstName()).orElse(""),
                    Optional.ofNullable(dossier.getUser().getLastName()).orElse("")).trim();
        }
        return "";
    }

    private ChampOperationResponseDto buildChampOperationDto(ChampOperation champ) {
        List<OptionChampOperationResponse> optionDtos = new ArrayList<>();
        if (champ.getOptions() != null) {
            for (OptionChampOperation option : champ.getOptions()) {
                optionDtos.add(OptionChampOperationResponse.builder()
                        .id(option.getId())
                        .label(option.getLabel())
                        .build());
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
