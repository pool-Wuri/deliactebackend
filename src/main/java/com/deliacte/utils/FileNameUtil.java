package com.deliacte.utils;

import java.text.Normalizer;
import java.time.Instant;

public final class FileNameUtil {

    private FileNameUtil() {
    }

    /**
     * Normalise un nom de fichier :
     * - supprime accents
     * - supprime caractères spéciaux
     * - remplace espaces par _
     * - met en MAJUSCULE
     * - ajoute timestamp en préfixe
     */
    public static String normalize(String originalFilename) {

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        // Séparer nom et extension
        String name = originalFilename;
        String extension = "";

        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            name = originalFilename.substring(0, lastDot);
            extension = originalFilename.substring(lastDot); // .pdf
        }

        // Supprimer accents
        name = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Remplacer espaces par _
        name = name.replaceAll("\\s+", "_");

        // Supprimer caractères spéciaux (garder lettres, chiffres, _)
        name = name.replaceAll("[^a-zA-Z0-9_]", "");

        // Mettre en MAJUSCULE
        name = name.toUpperCase();

        // Timestamp
        long timestamp = Instant.now().toEpochMilli();

        return timestamp + "_" + name + extension.toUpperCase();
    }

    public static String removeExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }


}
