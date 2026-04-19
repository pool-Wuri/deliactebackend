package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.response.DashboardStatsResponse;
import com.deliacte.dto.response.DashboardStatsResponse1;
import com.deliacte.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistiques", description = "API des statistiques et tableaux de bord")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/admin/dashboard")
    @Operation(summary = "Récupérer les statistiques du dashboard administrateur")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAdminDashboardStats() {
        return ResponseEntity.ok(statisticsService.getAdminDashboardStats());
    }


    @GetMapping("/stats")
    @Operation(summary = "Récupérer les statistiques générales du système")
    public ResponseEntity<ApiResponse<DashboardStatsResponse1>> getStats() {
        return ResponseEntity.ok(statisticsService.getStats());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CITOYEN') or #userId == authentication.principal.id")
    @Operation(summary = "Récupérer les statistiques d'un utilisateur")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getUserDashboardStats(@PathVariable UUID userId) {
        return ResponseEntity.ok(statisticsService.getUserDashboardStats(userId));
    }

    @GetMapping("/agent/{organisationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Récupérer les statistiques d'un agent pour son organisation")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAgentDashboardStats(@PathVariable UUID organisationId) {
        return ResponseEntity.ok(statisticsService.getAgentDashboardStats(organisationId));
    }
}
