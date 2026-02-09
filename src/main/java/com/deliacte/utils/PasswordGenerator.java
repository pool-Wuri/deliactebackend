package com.deliacte.utils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}<>?";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;

    private static final SecureRandom random = new SecureRandom();

    public static String generate(int length) {
        if (length < 12) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 12 caractères");
        }

        List<Character> password = new ArrayList<>();

        password.add(randomChar(UPPER));
        password.add(randomChar(LOWER));
        password.add(randomChar(DIGIT));
        password.add(randomChar(SPECIAL));

        for (int i = 4; i < length; i++) {
            password.add(randomChar(ALL));
        }

        Collections.shuffle(password, random);

        StringBuilder sb = new StringBuilder();
        password.forEach(sb::append);

        return sb.toString();
    }

    private static char randomChar(String source) {
        return source.charAt(random.nextInt(source.length()));
    }
}
