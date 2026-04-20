package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.OperationRequest;
import com.deliacte.dto.response.OperationResponse;
import com.deliacte.dto.response.ProcedureSimpleResponse;
import com.deliacte.dto.response.TypeOperationResponse;
import com.deliacte.entity.EntityObject;
import com.deliacte.entity.Operation;
import com.deliacte.entity.Procedure;
import com.deliacte.entity.TypeOperation;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.*;
import com.deliacte.service.OperationService;
import com.deliacte.utils.FileNameUtil;
import com.deliacte.utils.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierChampValue;
import com.deliacte.entity.DossierHistorique;
import com.deliacte.exception.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import de.phip1611.Docx4JSRUtil;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;
    private final ProcedureRepository procedureRepository;
    private final TypeOperationRepository typeOperationRepository;
    private final EntityObjectRepository entityObjectRepository;
    private  final DossierRepository dossierRepository;
    @Value("${file.outputs-dir}")
    private String DIRECTORY;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

    @Override
    public ApiResponse<OperationResponse> create(OperationRequest request) {
        Procedure procedure = procedureRepository.findByIdAndDeletedFalse(request.getProcedureId())
                .orElseThrow(() -> new ResourceNotFoundException("Procédure non trouvée"));

        TypeOperation typeOperation = null;
        if (request.getTypeOperationId() != null) {
            typeOperation = typeOperationRepository.findByIdAndDeletedFalse(request.getTypeOperationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
        }

        // Déterminer l'ordre
        Integer maxOrder = operationRepository.findMaxOrderIndexByProcedureId(procedure.getId());
        int newOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        Operation operation = Operation.builder()

                .name(request.getName())
                .description(request.getDescription())
                .orderIndex(newOrder)
                .procedure(procedure)
                .typeOperation(typeOperation)
                .isActive(request.getActive() != null ? request.getActive() : true)
                .build();

        operation = operationRepository.save(operation);

        return ApiResponse.success(mapToResponse(operation), "Opération créée avec succès");
    }

    @Override
    public ApiResponse<OperationResponse> update(UUID id, OperationRequest request) {
        Operation operation = operationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));

        if (request.getTypeOperationId() != null) {
            TypeOperation typeOperation = typeOperationRepository.findByIdAndDeletedFalse(request.getTypeOperationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'opération non trouvé"));
            operation.setTypeOperation(typeOperation);
        }


        operation.setName(request.getName());
        operation.setDescription(request.getDescription());
        operation.setIsCitizenOperation(request.getIsCitizenOperation());
        if (request.getActive() != null) {
            operation.setIsActive(request.getActive());
        }

        operation = operationRepository.save(operation);

        return ApiResponse.success(mapToResponse(operation), "Opération mise à jour avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OperationResponse> getById(UUID id) {
        Operation operation = operationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));

        return ApiResponse.success(mapToResponse(operation), "Opération récupérée avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<OperationResponse>> getAll(Pageable pageable) {
        Page<Operation> page = operationRepository.findAllByDeletedFalse(pageable);

        PageResponse<OperationResponse> response = PageResponse.<OperationResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return ApiResponse.success(response, "Liste des opérations récupérée avec succès");
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OperationResponse>>
    getOperationsFromUserOrganisations(UUID userId) {

        List<OperationResponse> content =
                operationRepository
                        .findOperationsFromUserOrganisations(userId)
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());

        return ApiResponse.success(
                PageResponse.of(content),
                "Opérations issues des organisations de l'utilisateur"
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OperationResponse>>
    getOperationsFromUserProcedures(UUID userId) {

        List<OperationResponse> content =
                operationRepository
                        .findOperationsFromUserProcedures(userId)
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());

        return ApiResponse.success(
                PageResponse.of(content),
                "Opérations issues des procédures de l'utilisateur"
        );
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OperationResponse>>
    getOperationsFromUserAgent(UUID userId) {

        List<OperationResponse> content =
                operationRepository
                        .findOperationsFromUserAgent(userId)
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());

        return ApiResponse.success(
                PageResponse.of(content),
                "Opérations issues des procédures de l'utilisateur"
        );
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OperationResponse>> getByProcedureId(UUID procedureId) {
        List<Operation> operations = operationRepository.findByProcedureIdAndDeletedFalseOrderByOrderIndexAsc(procedureId);

        List<OperationResponse> responses = operations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses, "Opérations de la procédure récupérées avec succès");
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OperationResponse>>
    getOperationsByOrganisationId(UUID organisationId) {

        List<OperationResponse> content = operationRepository
                .findOperationsByOrganisationId(organisationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(
                PageResponse.of(content),
                "Opérations de l'organisation récupérées avec succès"
        );
    }


    @Override
    public ApiResponse<Void> delete(UUID id) {
        Operation operation = operationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));

        operation.setDeleted(true);
        operationRepository.save(operation);

        return ApiResponse.<Void>success(null, "Opération supprimée avec succès");
    }

    @Override
    public ApiResponse<OperationResponse> activate(UUID id) {
        Operation operation = operationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));

        operation.setIsActive(true);
        operation = operationRepository.save(operation);

        return ApiResponse.success(mapToResponse(operation), "Opération activée avec succès");
    }

    @Override
    public ApiResponse<OperationResponse> deactivate(UUID id) {
        Operation operation = operationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));

        operation.setIsActive(false);
        operation = operationRepository.save(operation);

        return ApiResponse.success(mapToResponse(operation), "Opération désactivée avec succès");
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OperationResponse>> getNextOperations(UUID operationId) {

        List<OperationResponse> content =
                operationRepository.findNextOperations(operationId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(
                PageResponse.of(content),
                "Opérations suivantes récupérées avec succès"
        );
    }


    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PageResponse<OperationResponse>> getPreviousOperations(UUID operationId) {

        List<OperationResponse> content =
                operationRepository.findPreviousOperations(operationId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.success(
                PageResponse.of(content),
                "Opérations précédentes récupérées avec succès"
        );
    }


    // =====================================================
    // ➕ Ajouter des opérations suivantes
    // =====================================================
    @Transactional
    @Override
    public ApiResponse<Void> addNextOperations(UUID operationId, IdListRequest nextOperationIds) {

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération source introuvable"));

        List<Operation> nextOperations = operationRepository.findAllById(nextOperationIds.getIds());

        for (Operation next : nextOperations) {
            // éviter auto-référence
            if (next.getId().equals(operation.getId())) continue;

            operation.getNextOperations().add(next);
            next.getPreviousOperations().add(operation);
        }

        operationRepository.save(operation);

        return ApiResponse.success(null, "Opérations suivantes ajoutées avec succès");
    }

    // =====================================================
    // ➕ Ajouter des opérations précédentes
    // =====================================================
    @Transactional
    @Override
    public ApiResponse<Void> addPreviousOperations(UUID operationId, IdListRequest previousOperationIds) {

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération cible introuvable"));

        List<Operation> previousOperations = operationRepository.findAllById(previousOperationIds.getIds());

        for (Operation previous : previousOperations) {
            if (previous.getId().equals(operation.getId())) continue;

            operation.getPreviousOperations().add(previous);
            previous.getNextOperations().add(operation);
        }

        operationRepository.save(operation);

        return ApiResponse.success(null, "Opérations précédentes ajoutées avec succès");
    }

    // =====================================================
    // ➖ Retirer des opérations suivantes
    // =====================================================
    @Transactional
    @Override
    public ApiResponse<Void> removeNextOperations(UUID operationId, IdListRequest nextOperationIds) {

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération source introuvable"));

        List<Operation> nextOperations = operationRepository.findAllById(nextOperationIds.getIds());

        for (Operation next : nextOperations) {
            operation.getNextOperations().remove(next);
            next.getPreviousOperations().remove(operation);
        }

        operationRepository.save(operation);

        return ApiResponse.success(null, "Opérations suivantes retirées avec succès");
    }

    // =====================================================
    // ➖ Retirer des opérations précédentes
    // =====================================================
    @Transactional
    @Override
    public ApiResponse<Void> removePreviousOperations(UUID operationId, IdListRequest previousOperationIds) {

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération cible introuvable"));

        List<Operation> previousOperations = operationRepository.findAllById(previousOperationIds.getIds());

        for (Operation previous : previousOperations) {
            operation.getPreviousOperations().remove(previous);
            previous.getNextOperations().remove(operation);
        }

        operationRepository.save(operation);

        return ApiResponse.success(null, "Opérations précédentes retirées avec succès");
    }


    @Override
    public ApiResponse<Void> reorder(UUID procedureId, List<UUID> operationIds) {
        for (int i = 0; i < operationIds.size(); i++) {
            Operation operation = operationRepository.findByIdAndDeletedFalse(operationIds.get(i))
                    .orElseThrow(() -> new ResourceNotFoundException("Opération non trouvée"));
            operation.setOrderIndex(i + 1);
            operationRepository.save(operation);
        }

        return ApiResponse.<Void>success(null, "Ordre des opérations mis à jour avec succès");
    }


    @Transactional
    @Override
    public ApiResponse<Void> removeEntityObjectFromOperation(
            UUID operationId,
            UUID entityObjectId
    ) {
        // 1️⃣ Récupérer l'opération
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Opération introuvable"));

        // 2️⃣ Récupérer l'EntityObject
        EntityObject entityObject = entityObjectRepository.findById(entityObjectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("EntityObject introuvable"));

        // 3️⃣ Vérifier que l'entité est bien liée à l'opération
        if (!operation.getEntityObjects().contains(entityObject)) {
            throw new IllegalStateException(
                    "Cet EntityObject n'est pas associé à cette opération"
            );
        }

        // 5️⃣ Retirer l'EntityObject de l'opération
        operation.getEntityObjects().remove(entityObject);

        // 6️⃣ Sauvegarder
        operationRepository.save(operation);

        return ApiResponse.success(null,
                "EntityObject retiré de l'opération avec succès");
    }


    @Transactional
    @Override
    public ApiResponse<String> uploadTemplate(
            UUID operationId,
            MultipartFile templateFile
    ) {

        // 1️⃣ Vérifier l'opération
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Opération introuvable"));

        if (templateFile == null || templateFile.isEmpty()) {
            return ApiResponse.error("Fichier template invalide");
        }

        // 2️⃣ Dossier métier (par opération)
        String operationDir = DIRECTORY + File.separator + "operation_" + operationId;

        // 3️⃣ Enregistrer le fichier (nom normalisé + timestamp)
        String savedFilename = FileStorageUtil.saveFile(
                templateFile,
                operationDir
        );

        // 4️⃣ Associer le template à l'opération
        operation.setTemplateOutput(savedFilename);
        operationRepository.save(operation);

        // 5️⃣ Réponse
        return ApiResponse.success(
                savedFilename,
                "Template associé à l'opération avec succès"
        );
    }


    @Override
    public ApiResponse<String> generatePdfFromWordTemplate(UUID operationId, String numeroDossier) {
        try {
            byte[] bytes = generatePdfBytes(operationId, numeroDossier);
            // Écrire sur disque et retourner le chemin (usage interne / admin)
            Operation operation = operationRepository.findById(operationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable"));
            String baseName = FileNameUtil.removeExtension(operation.getTemplateOutput());
            String pdfPath  = DIRECTORY + File.separator + baseName + "_" + numeroDossier + ".pdf";
            Files.write(new File(pdfPath).toPath(), bytes);
            return ApiResponse.success(pdfPath, "PDF généré avec succès");
        } catch (Exception e) {
            return ApiResponse.error("Erreur : " + e.getMessage());
        }
    }

    /**
     * Génère le PDF de sortie pour un dossier à partir du template Word de l’opération.
     *
     * Les placeholders dans le template Word doivent respecter le format : ${CODE_CHAMP}
     * Exemples : ${PASS_NOM}, ${numero_dossier}, ${date_generation}, ${procedure_nom}
     *
     * Retourne les bytes du PDF prêts à être streamés au client.
     */
    @Override
    public byte[] generatePdfBytes(UUID operationId, String numeroDossier) {

        // ── 1. Charger l’opération et son template ────────────────────────────
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable : " + operationId));

        String templateFilename = operation.getTemplateOutput();
        if (templateFilename == null || templateFilename.isBlank()) {
            throw new IllegalStateException("Aucun template configuré pour l’opération : " + operation.getName());
        }

        File templateFile = new File(DIRECTORY + File.separator + templateFilename);
        if (!templateFile.exists()) {
            throw new IllegalStateException("Fichier template introuvable : " + templateFile.getAbsolutePath());
        }

        // ── 2. Charger le dossier et construire la map de remplacement ────────
        Dossier dossier = dossierRepository.findByDossierNumber(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable : " + numeroDossier));

        Map<String, String> placeholders = buildPlaceholders(dossier);

        // ── 3. Charger le Word, remplacer, enregistrer en DOCX temporaire ─────
        String baseName    = FileNameUtil.removeExtension(templateFilename);
        String tempDocxPath = DIRECTORY + File.separator + baseName + "_" + numeroDossier + "_tmp.docx";
        File   tempDocx    = new File(tempDocxPath);

        try {
            WordprocessingMLPackage pkg = WordprocessingMLPackage.load(templateFile);
            Docx4JSRUtil.searchAndReplace(pkg, placeholders);
            cleanPlaceholders(pkg);
            pkg.save(tempDocx);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du remplissage du template Word : " + e.getMessage(), e);
        }

        // ── 4. Convertir en PDF via LibreOffice ───────────────────────────────
        String outDir   = tempDocx.getParent();
        // LibreOffice génère le PDF avec le même nom de base que le DOCX
        String pdfName  = baseName + "_" + numeroDossier + "_tmp.pdf";
        File   pdfFile  = new File(outDir + File.separator + pdfName);

        if (pdfFile.exists()) pdfFile.delete();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "soffice", "--headless",
                    "--convert-to", "pdf",
                    "--outdir", outDir,
                    tempDocx.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            long t0 = System.currentTimeMillis();
            Process proc = pb.start();
            int code = proc.waitFor();
            if (code != 0) {
                throw new RuntimeException("LibreOffice a retourné le code " + code);
            }
            log.info("PDF généré en {} ms : {}", System.currentTimeMillis() - t0, pdfFile.getAbsolutePath());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Conversion PDF interrompue", e);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lancer LibreOffice : " + e.getMessage(), e);
        } finally {
            tempDocx.delete();
        }

        // ── 5. Lire les bytes et nettoyer ─────────────────────────────────────
        if (!pdfFile.exists()) {
            throw new RuntimeException("Le fichier PDF attendu n’a pas été produit : " + pdfFile.getAbsolutePath());
        }
        try {
            byte[] bytes = Files.readAllBytes(pdfFile.toPath());
            pdfFile.delete();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le PDF généré : " + e.getMessage(), e);
        }
    }

    /**
     * Construit la map des placeholders à partir des valeurs du dossier.
     * Clés : ${CODE_CHAMP}  — valeurs : texte brut ou chemin fichier.
     */
    private Map<String, String> buildPlaceholders(Dossier dossier) {
        Map<String, String> map = new HashMap<>();

        // Valeurs des champs (les plus récentes actives gagnent en cas de doublon)
        for (DossierHistorique historique : dossier.getHistoriques()) {
            for (DossierChampValue val : historique.getChampValues()) {
                if (!Boolean.TRUE.equals(val.getIsActive())) continue;
                String code = val.getChampOperation().getCode();
                String effectiveValue = (val.getValue() != null && !val.getValue().isBlank())
                        ? val.getValue()
                        : (val.getFilePath() != null ? val.getFilePath() : "");
                map.put("${" + code + "}", effectiveValue);
            }
        }

        // Placeholders système
        map.put("${numero_dossier}",  dossier.getDossierNumber());
        map.put("${date_generation}", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        map.put("${procedure_nom}",   dossier.getProcedure() != null ? dossier.getProcedure().getName() : "");

        if (dossier.getUser() != null) {
            map.put("${citoyen_nom}",    nvl(dossier.getUser().getLastName()));
            map.put("${citoyen_prenom}", nvl(dossier.getUser().getFirstName()));
            map.put("${citoyen_email}",  nvl(dossier.getUser().getEmail()));
        }

        if (dossier.getSubmittedAt() != null) {
            map.put("${date_soumission}", dossier.getSubmittedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        log.debug("Placeholders construits : {} entrées pour le dossier {}", map.size(), dossier.getDossierNumber());
        return map;
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    public static void cleanPlaceholders(WordprocessingMLPackage wordMLPackage) {
        // Parcourt tous les éléments du document
        new TraversalUtil(wordMLPackage.getMainDocumentPart().getContent(), new CallbackImpl() {
            @Override
            public List<Object> apply(Object o) {
                if (o instanceof Text) {
                    Text textElement = (Text) o;
                    String original = textElement.getValue();

                    // Applique la regex
                    Matcher matcher = PLACEHOLDER_PATTERN.matcher(original);
                    String cleaned = matcher.replaceAll("$1");

                    // Mets à jour uniquement si du changement est détecté
                    if (!original.equals(cleaned)) {
                        textElement.setValue(cleaned);
                    }
                }
                return null;
            }
        });
    }




    private OperationResponse mapToResponse(Operation operation) {
        OperationResponse.OperationResponseBuilder builder = OperationResponse.builder()
                .id(operation.getId())

                .name(operation.getName())
                .description(operation.getDescription())
                .orderIndex(operation.getOrderIndex())
                .active(operation.getIsActive())
                .isCitizenOperation(operation.getIsCitizenOperation())
                .templateUrl(operation.getTemplateOutput())
                .createdAt(operation.getCreatedAt())
                .updatedAt(operation.getUpdatedAt());


        if (operation.getProcedure() != null) {
            builder.procedureName(operation.getProcedure().getName());
            builder.procedureId(operation.getProcedure().getId());
            builder.procedure(ProcedureSimpleResponse.builder()
                    .id(operation.getProcedure().getId())
                    .code(operation.getProcedure().getCode())
                    .name(operation.getProcedure().getName())
                    .build());
        }

        if (operation.getTypeOperation() != null) {
            builder.typeOperation(TypeOperationResponse.builder()
                    .id(operation.getTypeOperation().getId())
                    .code(operation.getTypeOperation().getCode())
                    .libelle(operation.getTypeOperation().getLibelle())
                    .build());
        }

        return builder.build();
    }
}
