package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.DossierRequest;
import com.deliacte.dto.response.DossierListResponse;
import com.deliacte.dto.response.DossierResponse;
import com.deliacte.dto.response.OperationFormResponseDto;
import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierOperation;
import com.deliacte.enums.DossierOperationStatus;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.DossierService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/dossiers")
@RequiredArgsConstructor
public class DossierController {

    private final DossierService dossierService;

    @PostMapping("/submit")
    @Operation(
            summary = "Soumettre un dossier",
            description = "Permet à l'utilisateur authentifié de soumettre un dossier. "
                    + "Le dossier est traité selon l'opération courante et les valeurs des champs fournies."
    )    public ResponseEntity<DossierResponse> submitDossier(
            @Valid @RequestBody DossierRequest request) {

        UUID userId = SecurityUtils.getCurrentUserId();

        log.info("Réception d'une soumission de dossier pour l'utilisateur: {}", userId != null ? userId : "UNKNOWN");

        if (userId == null) {
            log.warn("Tentative de soumission sans utilisateur authentifié");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Dossier updatedDossier = dossierService.processDossierSubmission(request, userId);
            DossierResponse response = mapToDossierResponse(updatedDossier);
            log.info("Soumission traitée avec succès pour le dossier: {}", updatedDossier.getDossierNumber());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors du traitement de la soumission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @GetMapping("/me")
    @Operation(
            summary = "Lister mes dossiers",
            description = "Retourne la liste des dossiers de l'utilisateur authentifié, "
                    + "avec le numéro de dossier, le nom de la procédure, le statut actuel, "
                    + "la date de soumission et l'étape courante."
    )    public ApiResponse<List<DossierListResponse>> getMyDossiers(

    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return dossierService.getMyDossiers(userId);
    }

    @GetMapping("/operation/{operationId}")
    @Operation(
            summary = "Lister les dossiers par opération",
            description = "Retourne la liste des dossiers actuellement à une étape donnée (opération), "
                    + "avec le numéro de dossier, le nom de la procédure, le statut actuel, "
                    + "la date de soumission et l'étape courante."
    )
    public ApiResponse<List<DossierListResponse>> getDossiersByOperation(
            @PathVariable UUID operationId
    ) {
        return dossierService.getDossiersByOperation(operationId);
    }




    @GetMapping("/form/{numeroDossier}")
    @Operation(summary = "Charger le formulaire d'un dossier",
            description = "Retourne le formulaire d'une opération avec les valeurs saisies, à partir du numéro de dossier")
    public ApiResponse<OperationFormResponseDto> getForm(@PathVariable String numeroDossier) {
        return dossierService.getOperationFormByNumeroDossier(numeroDossier);
    }






    private DossierResponse mapToDossierResponse(Dossier dossier) {
        // Trouver l'opération en attente pour obtenir l'ID de l'opération actuelle
        UUID currentOperationId = dossier.getOperationSteps().stream()
                .filter(step -> step.getStatus() == DossierOperationStatus.PENDING)
                .map(DossierOperation::getOperation)
                .map(com.deliacte.entity.Operation::getId)
                .findFirst()
                .orElse(null);

        return DossierResponse.builder()
                .id(dossier.getId())
                .dossierNumber(dossier.getDossierNumber())
                .procedureId(dossier.getProcedure().getId())
                .currentOperationId(currentOperationId) // ID de l'opération actuelle
                .status(dossier.getStatus())
                .submittedAt(dossier.getSubmittedAt())
                .completedAt(dossier.getCompletedAt())
              //  .comment(dossier.getComment()) // Assurez-vous que le champ comment existe sur l'entité Dossier
                .userId(dossier.getUser().getId())
                .message("Dossier traité avec succès")
                .build();
    }
}
