package com.deliacte.enums;

public enum UserStatus {
    CITOYEN("Citoyen", "Pour les citoyens burkinabè"),
    RESIDENT("Résident", "Pour les résidents étrangers au Burkina Faso"),
    REFUGIE("Réfugié", "Pour les réfugiés"),
    DIPLOMATE("Diplomate", "Pour les diplomates accrédités au Burkina Faso"),
    ETRANGER("Étranger", "Pour les visiteurs étrangers au Burkina Faso");

    private final String label;
    private final String description;

    UserStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
