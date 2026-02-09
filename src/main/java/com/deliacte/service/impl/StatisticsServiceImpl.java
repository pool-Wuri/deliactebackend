package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.response.DashboardStatsResponse;
import com.deliacte.entity.User;
import com.deliacte.enums.DossierStatus;
import com.deliacte.enums.UserRole;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.DossierRepository;
import com.deliacte.repository.OrganisationRepository;
import com.deliacte.repository.ProcedureRepository;
import com.deliacte.repository.UserRepository;
import com.deliacte.repository.stats.*;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {
    
    private  final DossierStatsRepository dossierStatsRepository;
    private final OrganisationStatsRepository organisationStatsRepository;
    private  final ProcedureStatsRepository procedureStatsRepository;
    private  final UserStatsRepository userStatsRepository;
    private  final  UserRepository userRepository;
    private final OperationStatsRepository operationStatsRepository;

    @Override
    public ApiResponse<DashboardStatsResponse> getAdminDashboardStats() {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("Utilisateur non connecté");
        }

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur créateur non trouvé"));

        DashboardStatsResponse stats;

        switch (creator.getRole()) {
            case SUPER_ADMIN -> {
                stats = DashboardStatsResponse.builder()
                        .totalUsers(userStatsRepository.countAllUsers())
                        .totalOrganisations(organisationStatsRepository.countAllOrganisations())
                        .totalOperations(operationStatsRepository.countAllOperations())
                        .totalProcedures(procedureStatsRepository.countAllProcedures())
                        .dossiers(dossierStatsRepository.countAllDossiers())
                        .build();
            }
            case RESPONSABLE_ORGANISATION -> {
                stats = DashboardStatsResponse.builder()
                        .totalUsers(userStatsRepository.countUsersByOrganisationOfUser(currentUserId))
                        .totalOrganisations(organisationStatsRepository.countOrganisationsByResponsibleOrganisation(currentUserId))
                        .totalProcedures(procedureStatsRepository.countProceduresByOrganisationOfUser(currentUserId))
                        .totalOperations(operationStatsRepository.countOperationsByOrganisationOfUser(currentUserId))
                        .dossiers(dossierStatsRepository.countByUserOrganisations(currentUserId))
                        .build();
            }
            case ADMIN_PROCEDURE -> {
                stats = DashboardStatsResponse.builder()
                        .totalUsers(userStatsRepository.countUsersByProceduresOfUser(currentUserId))
                        .totalOrganisations(organisationStatsRepository.countOrganisationsByResponsibleProcedures(currentUserId))
                        .totalOperations(operationStatsRepository.countOperationsByProceduresOfUser(currentUserId))
                        .totalProcedures(procedureStatsRepository.countProceduresAssignedToUser(currentUserId))
                        .dossiers(dossierStatsRepository.countByUserProcedures(currentUserId))
                        .build();
            }
            default -> {
                return ApiResponse.error("Rôle non supporté pour les statistiques");
            }
        }

        return ApiResponse.success(stats, "Statistiques récupérées avec succès");
    }


    @Override
    public ApiResponse<DashboardStatsResponse> getUserDashboardStats(UUID userId) {
//        DashboardStatsResponse stats = DashboardStatsResponse.builder()
//                .totalDossiers(dossierRepository.countByUserIdAndDeletedFalse(userId))
//                .pendingDossiers(dossierRepository.countByUserIdAndStatusAndDeletedFalse(userId, DossierStatus.SUBMITTED))
//                .inProgressDossiers(dossierRepository.countByUserIdAndStatusAndDeletedFalse(userId, DossierStatus.IN_PROGRESS))
//                .completedDossiers(dossierRepository.countByUserIdAndStatusAndDeletedFalse(userId, DossierStatus.COMPLETED))
//                .rejectedDossiers(dossierRepository.countByUserIdAndStatusAndDeletedFalse(userId, DossierStatus.REJECTED))
//                .build();
//
        return ApiResponse.success(null, "Statistiques utilisateur récupérées avec succès");
    }
    
    @Override
    public ApiResponse<DashboardStatsResponse> getAgentDashboardStats(UUID organisationId) {
//        DashboardStatsResponse stats = DashboardStatsResponse.builder()
//                .totalDossiers(dossierRepository.countByOrganisationIdAndDeletedFalse(organisationId))
//                .pendingDossiers(dossierRepository.countByOrganisationIdAndStatusAndDeletedFalse(organisationId, DossierStatus.SUBMITTED))
//                .inProgressDossiers(dossierRepository.countByOrganisationIdAndStatusAndDeletedFalse(organisationId, DossierStatus.IN_PROGRESS))
//                .completedDossiers(dossierRepository.countByOrganisationIdAndStatusAndDeletedFalse(organisationId, DossierStatus.COMPLETED))
//                .rejectedDossiers(dossierRepository.countByOrganisationIdAndStatusAndDeletedFalse(organisationId, DossierStatus.REJECTED))
//                .build();
        
        return ApiResponse.success(null, "Statistiques agent récupérées avec succès");
    }
}
