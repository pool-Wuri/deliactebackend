package com.deliacte.dto;

public interface DossierStatsProjection {

    long getTotalDossiers();

    long getDossiersEnAttente();

    long getDossiersEnCours();

    long getDossiersTermines();

    long getDossiersRejetes();
}
