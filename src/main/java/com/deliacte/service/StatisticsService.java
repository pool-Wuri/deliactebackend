package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.response.DashboardStatsResponse;

import java.util.UUID;

public interface StatisticsService {

    ApiResponse<DashboardStatsResponse> getAdminDashboardStats();

    ApiResponse<DashboardStatsResponse> getUserDashboardStats(UUID userId);

    ApiResponse<DashboardStatsResponse> getAgentDashboardStats(UUID organisationId);
}
