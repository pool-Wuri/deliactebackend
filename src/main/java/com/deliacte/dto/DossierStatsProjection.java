package com.deliacte.dto;

public interface DossierStatsProjection {
    Long getTotalDossiers();    // Au lieu de long
    Long getDossiersEnCours();
    Long getDossiersTermines();
    Long getDossiersRejetes();
    Long getDossiersEnAttente(); // Au lieu de long
}
