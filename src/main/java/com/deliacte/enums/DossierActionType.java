package com.deliacte.enums;

/**
 * Énumération des types d'actions possibles sur un dossier.
 * Utilisée pour tracer exactement ce qui s'est passé lors de chaque entrée d'historique.
 */
public enum DossierActionType {
    CREATE("Création du dossier"),
    SUBMIT("Soumission à l'opération suivante"),
    REJECT("Rejet vers une opération précédente"),
    APPROVE("Approbation"),
    REVIEW("Révision"),
    COMPLETE("Completion"),
    CANCEL("Annulation");

    private final String description;

    DossierActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
