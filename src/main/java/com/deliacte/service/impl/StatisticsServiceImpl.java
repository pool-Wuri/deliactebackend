package com.deliacte.service.impl;

import com.deliacte.dto.*;
import com.deliacte.dto.response.*;
import com.deliacte.entity.User;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.UserRepository;
import com.deliacte.repository.stats.*;
import com.deliacte.security.SecurityUtils;
import com.deliacte.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public ApiResponse<DashboardStatsResponse1> getStats() {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return ApiResponse.error("Utilisateur non connecté");
        }

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        DashboardStatsResponse1 stats;

        switch (creator.getRole()) {
            case SUPER_ADMIN -> {
                DossierStatsProjection dossierStats = dossierStatsRepository.countAllDossiers();

                stats = DashboardStatsResponse1.builder()
                        .totalUsers(userStatsRepository.countAllUsers())
                        .totalOrganisations(organisationStatsRepository.countAllOrganisations())
                        .totalProcedures(procedureStatsRepository.countAllProcedures())
                        .dossiers(DossierStatsDto.fromProjection(dossierStats))
                        .monthlyCreated(dossierStatsRepository.monthlyCreated())
                        .monthlyCompleted(dossierStatsRepository.monthlyCompleted())
                        .monthlyPending(dossierStatsRepository.monthlyPending())
                        .monthlyRejected(dossierStatsRepository.monthlyRejected())
                        .monthlySkipped(dossierStatsRepository.monthlySkipped())
                        .repartitionParType(dossierStatsRepository.countByProcedure())
                        .dossiersParOrganisation(dossierStatsRepository.countByOrganisation())
                        .build();
            }
            case RESPONSABLE_ORGANISATION -> {
                DossierStatsProjection dossierStats = dossierStatsRepository.countByUserOrganisations(currentUserId);

                stats = DashboardStatsResponse1.builder()
                        .totalUsers(userStatsRepository.countUsersByOrganisationOfUser(currentUserId))
                        .totalOrganisations(organisationStatsRepository.countOrganisationsByResponsibleOrganisation(currentUserId))
                        .totalProcedures(procedureStatsRepository.countProceduresByOrganisationOfUser(currentUserId))
                        .dossiers(DossierStatsDto.fromProjection(dossierStats))
                        .monthlyCreated(dossierStatsRepository.monthlyCreatedByUserOrganisations(currentUserId))
                        .monthlyCompleted(dossierStatsRepository.monthlyCompletedByUserOrganisations(currentUserId))
                        .monthlyPending(dossierStatsRepository.monthlyPendingByUserOrganisations(currentUserId))
                        .monthlyRejected(dossierStatsRepository.monthlyRejectedByUserOrganisations(currentUserId))
                        .monthlySkipped(dossierStatsRepository.monthlySkippedByUserOrganisations(currentUserId))
                        .repartitionParType(dossierStatsRepository.countByProcedureUserOrganisations(currentUserId))
                        .dossiersParOrganisation(dossierStatsRepository.countByOrganisationUserOrganisations(currentUserId))
                        .build();
            }
            case ADMIN_PROCEDURE -> {
                DossierStatsProjection dossierStats = dossierStatsRepository.countByUserProcedures(currentUserId);

                stats = DashboardStatsResponse1.builder()
                        .totalUsers(userStatsRepository.countUsersByProceduresOfUser(currentUserId))
                        .totalOrganisations(organisationStatsRepository.countOrganisationsByResponsibleProcedures(currentUserId))
                        .totalProcedures(procedureStatsRepository.countProceduresAssignedToUser(currentUserId))
                        .dossiers(DossierStatsDto.fromProjection(dossierStats))
                        .monthlyCreated(dossierStatsRepository.monthlyCreatedByUserProcedures(currentUserId))
                        .monthlyCompleted(dossierStatsRepository.monthlyCompletedByUserProcedures(currentUserId))
                        .monthlyPending(dossierStatsRepository.monthlyPendingByUserProcedures(currentUserId))
                        .monthlyRejected(dossierStatsRepository.monthlyRejectedByUserProcedures(currentUserId))
                        .monthlySkipped(dossierStatsRepository.monthlySkippedByUserProcedures(currentUserId))
                        .repartitionParType(dossierStatsRepository.countByProcedureUserProcedures(currentUserId))
                        .dossiersParOrganisation(dossierStatsRepository.countByOrganisationUserProcedures(currentUserId))
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
