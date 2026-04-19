package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.response.DashboardStatsResponse;
import com.deliacte.dto.response.DashboardStatsResponse1;

import java.util.UUID;

public interface StatisticsService {

    ApiResponse<DashboardStatsResponse> getAdminDashboardStats();

    ApiResponse<DashboardStatsResponse1> getStats();

    ApiResponse<DashboardStatsResponse> getUserDashboardStats(UUID userId);

    ApiResponse<DashboardStatsResponse> getAgentDashboardStats(UUID organisationId);
}
