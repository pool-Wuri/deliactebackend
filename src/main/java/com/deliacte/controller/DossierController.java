package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.DossierRequest;
import com.deliacte.dto.response.DossierListResponse;
import com.deliacte.dto.response.DossierResponse;
import com.deliacte.dto.response.DossierTimelineResponse;
import com.deliacte.dto.response.OperationFormResponseDto;
import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierOperation;
import com.deliacte.entity.Payment;
import com.deliacte.enums.DossierOperationStatus;
import com.deliacte.repository.DossierOperationRepository;
import com.deliacte.repository.DossierRepository;
import com.deliacte.repository.PaymentRepository;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.DossierService;
import com.deliacte.service.OperationService;
import com.deliacte.utils.FileStorageUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/dossiers")
@RequiredArgsConstructor
public class DossierController {

    private final DossierService dossierService;
    private final OperationService operationService;
    private final DossierRepository dossierRepository;
    private final DossierOperationRepository dossierOperationRepository;
    private final PaymentRepository paymentRepository;

    @Value("${file.outputs-dir}")
    private String outputsDir;

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Message", e.getMessage() != null ? e.getMessage() : "Erreur inconnue")
                    .build();
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




    @PostMapping("/upload-file")
    @Operation(
            summary = "Uploader un fichier pour un dossier",
            description = "Enregistre un fichier sur le serveur avant la soumission du dossier. "
                    + "Retourne le chemin relatif du fichier stocké à inclure dans champValues.filePath."
    )
    public ResponseEntity<ApiResponse<String>> uploadDossierFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "champId", required = false) String champId) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Fichier vide ou manquant"));
        }

        try {
            String subDir = champId != null ? "champ_" + champId : "divers";
            String storageDir = outputsDir + File.separator + "fichiers" + File.separator + subDir;
            String savedFilename = FileStorageUtil.saveFile(file, storageDir);
            String relativePath = "fichiers" + File.separator + subDir + File.separator + savedFilename;
            log.info("Fichier dossier uploadé : {}", relativePath);
            return ResponseEntity.ok(ApiResponse.success(relativePath, "Fichier enregistré avec succès"));
        } catch (Exception e) {
            log.error("Erreur upload fichier dossier : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de l'enregistrement du fichier"));
        }
    }

    @GetMapping("/form/{numeroDossier}")
    @Operation(summary = "Charger le formulaire d'un dossier",
            description = "Retourne le formulaire d'une opération avec les valeurs saisies, à partir du numéro de dossier")
    public ApiResponse<OperationFormResponseDto> getForm(@PathVariable String numeroDossier) {
        return dossierService.getOperationFormByNumeroDossier(numeroDossier);
    }

    @GetMapping("/{dossierNumber}/timeline")
    @Operation(summary = "Timeline d'un dossier",
            description = "Retourne la chaîne complète des opérations d'un dossier avec leur statut (PENDING, COMPLETED, REJECTED, SKIPPED, ou null si non atteinte)")
    public ApiResponse<DossierTimelineResponse> getTimeline(@PathVariable String dossierNumber) {
        return dossierService.getDossierTimeline(dossierNumber);
    }

    @GetMapping("/{dossierNumber}/citizen-pending-form")
    @Operation(summary = "Formulaire de l'étape citoyenne en cours",
            description = "Retourne le formulaire de l'étape citoyenne PENDING du dossier, avec les valeurs déjà saisies pré-remplies et le commentaire de rejet s'il y en a un.")
    public ApiResponse<OperationFormResponseDto> getCitizenPendingForm(@PathVariable String dossierNumber) {
        return dossierService.getCitizenPendingForm(dossierNumber);
    }

    @GetMapping("/{dossierNumber}/operations/{operationId}/agent-form")
    @Operation(summary = "Formulaire agent pré-rempli",
            description = "Retourne les champs de l'opération pré-remplis avec les valeurs déjà saisies dans le dossier.")
    public ApiResponse<OperationFormResponseDto> getAgentForm(
            @PathVariable String dossierNumber,
            @PathVariable UUID operationId) {
        return dossierService.getOperationFormForAgent(dossierNumber, operationId);
    }

    @GetMapping("/{dossierNumber}/operations/{operationId}/output")
    @Operation(summary = "Télécharger le PDF généré pour une opération",
            description = "Génère et retourne le PDF. Retourne 402 si un paiement est requis et non effectué.")
    public ResponseEntity<byte[]> downloadOperationOutput(
            @PathVariable String dossierNumber,
            @PathVariable UUID operationId) {
        try {
            // ── Vérification paiement ────────────────────────────────────────
            Dossier dossier = dossierRepository.findByDossierNumber(dossierNumber)
                    .orElseThrow(() -> new RuntimeException("Dossier introuvable : " + dossierNumber));

            boolean procPaymentRequired = Boolean.TRUE.equals(dossier.getProcedure().getHasPayment());
            if (procPaymentRequired) {
                boolean paid = paymentRepository.findByDossier(dossier)
                        .map(p -> "SUCCESS".equalsIgnoreCase(p.getStatus())
                                && "COMPLETED".equalsIgnoreCase(p.getPaymentInfo()))
                        .orElse(false);
                if (!paid) {
                    log.warn("Accès refusé au document — paiement procédure requis pour dossier {}", dossierNumber);
                    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
                }
            }

            // Chercher la DossierOperation pour vérifier le paiement opération
            dossierOperationRepository.findByDossierIdOrderByReceivedAtAsc(dossier.getId())
                    .stream()
                    .filter(ds -> ds.getOperation().getId().equals(operationId))
                    .findFirst()
                    .ifPresent(ds -> {
                        if (Boolean.TRUE.equals(ds.getOperation().getHasPayment())) {
                            boolean paid = paymentRepository.findByDossierOperation(ds)
                                    .map(p -> "SUCCESS".equalsIgnoreCase(p.getStatus())
                                            && "COMPLETED".equalsIgnoreCase(p.getPaymentInfo()))
                                    .orElse(false);
                            if (!paid) {
                                throw new PaymentRequiredException("Paiement requis pour télécharger ce document");
                            }
                        }
                    });

            // ── Génération PDF ───────────────────────────────────────────────
            byte[] pdfBytes = operationService.generatePdfBytes(operationId, dossierNumber);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "output-" + dossierNumber + "-" + operationId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (PaymentRequiredException e) {
            log.warn("Accès refusé au document — paiement opération requis pour dossier {} op {}", dossierNumber, operationId);
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (Exception e) {
            log.error("Erreur génération PDF output pour dossier {} op {}: {}", dossierNumber, operationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** Exception locale pour signaler un paiement manquant sans polluer les autres catch */
    private static class PaymentRequiredException extends RuntimeException {
        PaymentRequiredException(String msg) { super(msg); }
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
