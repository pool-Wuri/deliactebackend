package com.deliacte.dto.response;

import com.deliacte.dto.MonthlyStatusProjection;
import com.deliacte.dto.OrganisationStatsProjection;
import com.deliacte.dto.TypeStatsProjection;
import com.deliacte.dto.MonthlyCreatedProjection;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse1 {

    private Long totalUsers;
    private Long totalOrganisations;
    private Long totalProcedures;

    private DossierStatsDto dossiers;

    // Stats mensuelles séparées
    private List<MonthlyCreatedProjection> monthlyCreated;      // Dossiers créés
    private List<MonthlyStatusProjection> monthlyCompleted;    // Dossiers terminés (COMPLETED)
    private List<MonthlyStatusProjection> monthlyPending;      // Dossiers en attente (PENDING)
    private List<MonthlyStatusProjection> monthlyRejected;     // Dossiers rejetés (REJECTED)
    private List<MonthlyStatusProjection> monthlySkipped;      // Dossiers sautés (SKIPPED)


    private List<TypeStatsProjection> repartitionParType;
    private List<OrganisationStatsProjection> dossiersParOrganisation;
}
