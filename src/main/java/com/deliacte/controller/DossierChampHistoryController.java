package com.deliacte.controller;

import com.deliacte.dto.response.DossierChampValueHistoryDto;
import com.deliacte.entity.DossierChampValue;
import com.deliacte.repository.DossierChampValueRepository;
import com.deliacte.service.DossierChampValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dossier-champ-history")
@RequiredArgsConstructor
public class DossierChampHistoryController {

    private final DossierChampValueService dossierChampValueService;

    /**
     * Récupère l'historique complet (toutes les versions) d'un champ pour un dossier.
     * Cela permet d'afficher comment la valeur a évolué au fil du temps.
     *
     * @param dossierId L'ID du dossier
     * @param champOperationId L'ID du champ
     * @return La liste de toutes les versions du champ, triées de la plus récente à la plus ancienne
     */
    @GetMapping("/dossier/{dossierId}/champ/{champOperationId}")
    public ResponseEntity<List<DossierChampValueHistoryDto>> getChampHistory(
            @PathVariable UUID dossierId,
            @PathVariable UUID champOperationId) {

        List<DossierChampValue> allVersions = dossierChampValueService.getAllVersions(dossierId, champOperationId);

        List<DossierChampValueHistoryDto> historyDtos = allVersions.stream()
                .map(this::mapToHistoryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historyDtos);
    }

    /**
     * Récupère la valeur ACTIVE (courante) d'un champ pour un dossier.
     * C'est la valeur qui est actuellement utilisée dans le dossier.
     */
    @GetMapping("/dossier/{dossierId}/champ/{champOperationId}/active")
    public ResponseEntity<DossierChampValueHistoryDto> getActiveChampValue(
            @PathVariable UUID dossierId,
            @PathVariable UUID champOperationId) {

        DossierChampValue activeValue = dossierChampValueService.getActiveValue(dossierId, champOperationId);

        if (activeValue == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToHistoryDto(activeValue));
    }

    private DossierChampValueHistoryDto mapToHistoryDto(DossierChampValue value) {
        return DossierChampValueHistoryDto.builder()
                .id(value.getId())
                .champOperationId(value.getChampOperation().getId())
                .champLabel(value.getChampOperation().getLabel())
                .value(value.getValue())
                .filePath(value.getFilePath())
                .version(value.getVersion())
                .isActive(value.getIsActive())
                .createdAt(value.getDossierHistorique().getActionDate())
                .submittedBy(value.getDossierHistorique().getUser().getFirstName() + " " + value.getDossierHistorique().getUser().getLastName())
                .actionComment(value.getDossierHistorique().getComment())
                .build();
    }
}
