package com.deliacte.enums;

public enum DossierOperationStatus {
    PENDING,    // En attente de traitement par l'opération
    COMPLETED,  // L'opération a terminé son travail sur ce dossier
    REJECTED,   // L'opération a rejeté le dossier
    SKIPPED     // L'étape a été sautée
}
