package com.deliacte.service;

import com.deliacte.entity.Operation;
import com.deliacte.entity.User;
import org.springframework.scheduling.annotation.Async;

import java.util.Set;

public interface EmailService {




    void sendPasswordEmail(String to, User user, String rawPassword);

    /**
     * Envoie un email de vérification de compte
     */
    void sendVerificationEmail(String to, String firstName, String verificationToken);

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    void sendPasswordResetEmail(String to, String firstName, String resetToken);

    /**
     * Envoie un email de bienvenue après activation du compte
     */
    void sendWelcomeEmail(String to, String firstName);

    /**
     * Envoie une notification de changement de mot de passe
     */
    void sendPasswordChangedEmail(String to, String firstName);

    @Async
    void sendOperationsAssignedEmail(User user, Set<Operation> operations);

    /**
     * Envoie une notification de nouveau dossier soumis
     */
    void sendDossierSubmittedEmail(String to, String firstName, String dossierNumber, String procedureName);

    /**
     * Envoie une notification de changement de statut de dossier
     */
    void sendDossierStatusChangedEmail(String to, String firstName, String dossierNumber, String newStatus);

    /**
     * Envoie un email générique
     */
    void sendEmail(String to, String subject, String htmlContent);
}
