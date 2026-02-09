package com.deliacte.utils;
import java.util.concurrent.ThreadLocalRandom;

public class CodeGenerator {

    public static String generateNumDossier() {
        // temps en ms encodé en base36
        String timePart = Long.toString(System.currentTimeMillis(), 36).toUpperCase();

        // partie aléatoire base36
        long random = ThreadLocalRandom.current().nextLong(36L * 36 * 36 * 36);
        String randomPart = Long.toString(random, 36).toUpperCase();

        // assemblage
        String code = timePart + randomPart;

        // padding ou découpe pour garantir 16 caractères
        if (code.length() < 16) {
            code = String.format("%16s", code).replace(' ', '0');
        } else if (code.length() > 16) {
            code = code.substring(0, 16);
        }

        return code;
    }
}
