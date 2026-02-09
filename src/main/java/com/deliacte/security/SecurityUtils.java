package com.deliacte.security;

import com.deliacte.entity.User;
import com.deliacte.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
        // Classe utilitaire
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public static UUID getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser()
                .map(User::getEmail)
                .orElse(null);
    }

    /**
     * Récupère le rôle de l'utilisateur connecté
     */
    public static UserRole getCurrentUserRole() {
        return getCurrentUser()
                .map(User::getRole)
                .orElse(null);
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    public static Boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle donné
     */
    public static Boolean hasRole(UserRole role) {
        return getCurrentUserRole() == role;
    }
}
