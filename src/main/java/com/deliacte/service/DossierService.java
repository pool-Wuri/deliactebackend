package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.DossierRequest;
import com.deliacte.dto.response.DossierListResponse;
import com.deliacte.dto.response.DossierResponse;
import com.deliacte.dto.response.DossierTimelineResponse;
import com.deliacte.dto.response.MyDossierResponseDto;
import com.deliacte.dto.response.OperationFormResponseDto;
import com.deliacte.entity.Dossier;
import com.deliacte.entity.User;
import com.deliacte.enums.DossierStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DossierService {


    @Transactional
    Dossier processDossierSubmission(DossierRequest request, UUID currentUser);

    @Transactional(readOnly = true)
    ApiResponse<List<DossierListResponse>> getMyDossiers(UUID userId);

    @Transactional(readOnly = true)
    ApiResponse<List<DossierListResponse>> getDossiersByOperation(UUID operationId);

    ApiResponse<OperationFormResponseDto> getOperationFormByNumeroDossier(String numeroDossier);

    @Transactional(readOnly = true)
    ApiResponse<DossierTimelineResponse> getDossierTimeline(String dossierNumber);
}
