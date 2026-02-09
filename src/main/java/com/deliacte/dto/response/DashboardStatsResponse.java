package com.deliacte.dto.response;

import com.deliacte.dto.DossierStatsProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Statistiques utilisateurs
    private Long totalUsers;
    private Long activeUsers;
    
    // Statistiques organisations
    private Long totalOrganisations;
    private Long activeOrganisations;
    
    // Statistiques procédures
    private Long totalProcedures;
    private Long publishedProcedures;


    // Statistiques dossiers
    private DossierStatsProjection dossiers;


    // Statistiques sur les operations
    private Long totalOperations;

    // Statistiques détaillées (optionnelles)
    private Map<String, Long> dossiersByStatus;
    private Map<String, Long> dossiersByMonth;
    private Map<String, Long> usersByRole;
    private Map<String, Long> proceduresByStatus;
}
