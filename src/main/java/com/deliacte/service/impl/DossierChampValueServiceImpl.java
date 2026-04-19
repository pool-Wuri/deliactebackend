package com.deliacte.service.impl;

import com.deliacte.entity.ChampOperation;
import com.deliacte.entity.DossierChampValue;
import com.deliacte.entity.DossierHistorique;
import com.deliacte.repository.ChampOperationRepository;
import com.deliacte.repository.DossierChampValueRepository;
import com.deliacte.dto.request.DossierChampValueRequest;
import com.deliacte.service.DossierChampValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierChampValueServiceImpl implements DossierChampValueService {

    private final DossierChampValueRepository dossierChampValueRepository;
    private final ChampOperationRepository champOperationRepository;

    @Override
    @Transactional
    public DossierChampValue addOrUpdateChampValue(DossierChampValueRequest request, DossierHistorique dossierHistorique) {
        log.info("Ajout/mise à jour de la valeur du champ: {}", request.getChampOperationId());

        ChampOperation champ = champOperationRepository.findById(request.getChampOperationId())
                .orElseThrow(() -> new RuntimeException("Champ non trouvé avec l'ID: " + request.getChampOperationId()));

        UUID dossierId = dossierHistorique.getDossier().getId();

        DossierChampValue currentActiveValue = dossierChampValueRepository
                .findActiveValueByDossierAndChamp(dossierId, request.getChampOperationId())
                .orElse(null);

        if (currentActiveValue != null) {
            log.info("Désactivation de la version {} du champ: {}", currentActiveValue.getVersion(), request.getChampOperationId());
            currentActiveValue.setIsActive(false);
            dossierChampValueRepository.save(currentActiveValue);
        }

        Integer nextVersion = (currentActiveValue != null) ? currentActiveValue.getVersion() + 1 : 1;

        DossierChampValue newValue = DossierChampValue.builder()
                .champOperation(champ)
                .value(request.getValue())
                .filePath(request.getFilePath())
                .dossierHistorique(dossierHistorique)
                .version(nextVersion)
                .isActive(true)
                .build();

        DossierChampValue savedValue = dossierChampValueRepository.save(newValue);
        log.info("Nouvelle version {} créée pour le champ: {}", nextVersion, request.getChampOperationId());

        return savedValue;
    }

    @Override
    public DossierChampValue getActiveValue(UUID dossierId, UUID champOperationId) {
        return dossierChampValueRepository.findActiveValueByDossierAndChamp(dossierId, champOperationId)
                .orElse(null);
    }

    @Override
    public List<DossierChampValue> getAllVersions(UUID dossierId, UUID champOperationId) {
        return dossierChampValueRepository.findAllVersionsByDossierAndChamp(dossierId, champOperationId);
    }

    @Override
    public List<DossierChampValue> getAllActiveValuesByDossier(UUID dossierId) {
        return List.of();
    }

    @Override
    public List<DossierChampValue> getValuesByHistorique(UUID dossierHistoriqueId) {
        return List.of();
    }

    @Override
    public void deactivateAllValuesByDossier(UUID dossierId) {

    }

    @Override
    public Optional<DossierChampValue> findActiveValue(UUID dossierId, UUID champOperationId) {
        return Optional.empty();
    }

    @Override
    public Integer countVersions(UUID dossierId, UUID champOperationId) {
        return 0;
    }
}
