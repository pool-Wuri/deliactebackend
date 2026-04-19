package com.deliacte.dto.response;

import com.deliacte.dto.DossierStatsProjection;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DossierStatsDto {

    private Long totalDossiers;
    private Long dossiersEnAttente;
    private Long dossiersRejetes;
    private Long dossiersTermines;

    /**
     * Méthode utilitaire pour convertir une projection en DTO
     */
    public static DossierStatsDto fromProjection(DossierStatsProjection projection) {
        if (projection == null) {
            return new DossierStatsDto(0L, 0L, 0L, 0L);
        }
        return DossierStatsDto.builder()
                .totalDossiers(projection.getTotalDossiers())
                .dossiersEnAttente(projection.getDossiersEnAttente())
                .dossiersRejetes(projection.getDossiersRejetes())
                .dossiersTermines(projection.getDossiersTermines())
                .build();
    }
}
