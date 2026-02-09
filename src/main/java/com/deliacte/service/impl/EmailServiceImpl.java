package com.deliacte.service.impl;

import com.deliacte.entity.Operation;
import com.deliacte.entity.User;
import com.deliacte.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendPasswordEmail(String to, User user, String rawPassword) {
        String subject = "Vos identifiants de connexion Deliacte";
        String loginLink = frontendUrl + "/auth/login";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; padding: 0; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 40px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; letter-spacing: 1px; }
                    .header p { margin: 10px 0 0; opacity: 0.9; font-size: 14px; }
                    .content { background: #ffffff; padding: 40px; }
                    .welcome-text { font-size: 18px; font-weight: bold; color: #006600; margin-bottom: 20px; }
                    .credentials-box { background: #f4fbf4; border: 1px dashed #006600; border-radius: 8px; padding: 25px; margin: 25px 0; text-align: center; }
                    .credential-item { margin: 10px 0; }
                    .label { font-size: 12px; text-transform: uppercase; color: #666; display: block; margin-bottom: 4px; }
                    .value { font-family: 'Courier New', Courier, monospace; font-size: 20px; font-weight: bold; color: #333; letter-spacing: 1px; }
                    .button { display: inline-block; background: #006600; color: #ffffff !important; padding: 16px 35px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; transition: background 0.3s ease; }
                    .warning { background: #fff8f1; border-left: 4px solid #ff9800; padding: 15px; margin-top: 25px; font-size: 13px; color: #856404; }
                    .footer { background: #f9f9f9; text-align: center; padding: 20px; color: #888; font-size: 12px; border-top: 1px solid #eee; }
                    .social-links { margin-bottom: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Deliacte</h1>
                        <p>Plateforme de Services Gouvernementaux</p>
                    </div>
                    <div class="content">
                        <div class="welcome-text">Bonjour %s,</div>
                        <p>Votre compte a été créé avec succès sur la plateforme <strong>Deliacte</strong>. Voici vos identifiants de connexion sécurisés :</p>
                        
                        <div class="credentials-box">
                            <div class="credential-item">
                                <span class="label">Identifiant (Email)</span>
                                <span class="value" style="font-size: 16px;">%s</span>
                            </div>
                            <div style="height: 15px;"></div>
                            <div class="credential-item">
                                <span class="label">Mot de passe temporaire</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <p style="text-align: center;">
                            <a href="%s" class="button">Se connecter à mon espace</a>
                        </p>

                        <div class="warning">
                            <strong>Sécurité :</strong> Pour des raisons de sécurité, nous vous recommandons vivement de modifier ce mot de passe dès votre première connexion.
                        </div>

                        <p style="margin-top: 30px; font-size: 14px;">Si vous rencontrez des difficultés, n'hésitez pas à contacter notre support technique.</p>
                    </div>
                    <div class="footer">
                        <p>Ceci est un message automatique, merci de ne pas y répondre.</p>
                        <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur",
                user.getEmail(),
                rawPassword, // On suppose que le mot de passe en clair est passé ici avant hachage
                loginLink
        );

        sendEmail(to, subject, htmlContent);
    }
    @Override
    @Async
    public void sendVerificationEmail(String to, String firstName, String verificationToken) {
        System.out.println("envoie de mail pour la prmeire fosi");
        String subject = "Vérification de votre compte Deliacte";
        String verificationLink = frontendUrl + "/auth/verify?token=" + verificationToken;

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; padding: 0; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 40px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; letter-spacing: 1px; }
                    .header p { margin: 10px 0 0; opacity: 0.9; font-size: 14px; }
                    .content { background: #ffffff; padding: 40px; }
                    .welcome-text { font-size: 20px; font-weight: bold; color: #006600; margin-bottom: 20px; }
                    .info-box { background: #f4fbf4; border-radius: 8px; padding: 25px; margin: 25px 0; border-left: 4px solid #006600; }
                    .button { display: inline-block; background: #006600; color: #ffffff !important; padding: 16px 35px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; transition: background 0.3s ease; }
                    .link-alt { font-size: 12px; color: #666; margin-top: 25px; padding-top: 20px; border-top: 1px solid #eee; }
                    .footer { background: #f9f9f9; text-align: center; padding: 20px; color: #888; font-size: 12px; border-top: 1px solid #eee; }
                    .expiry-note { font-size: 13px; color: #d32f2f; font-weight: 500; margin-top: 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Deliacte</h1>
                        <p>Plateforme de Services Gouvernementaux</p>
                    </div>
                    <div class="content">
                        <div class="welcome-text">Bienvenue sur Deliacte, %s !</div>
                        
                        <div class="info-box">
                            <p style="margin: 0;">Merci de vous être inscrit. Pour activer votre compte et accéder à l'ensemble de nos services, une dernière étape est nécessaire : la vérification de votre adresse email.</p>
                        </div>

                        <p style="text-align: center;">
                            <a href="%s" class="button">Vérifier mon compte</a>
                        </p>

                        <p class="expiry-note">⚠️ Ce lien de vérification expirera dans 24 heures.</p>

                        <div class="link-alt">
                            <p>Si le bouton ne fonctionne pas, copiez et collez ce lien dans votre navigateur :</p>
                            <p style="word-break: break-all; color: #006600; font-family: monospace;">%s</p>
                        </div>

                        <p style="margin-top: 30px; font-size: 14px; color: #666;">Si vous n'avez pas créé de compte sur notre plateforme, vous pouvez ignorer cet email en toute sécurité.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                firstName != null ? firstName : "Utilisateur",
                verificationLink,
                verificationLink
        );

        sendEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String firstName, String resetToken) {
        String subject = "Réinitialisation de votre mot de passe Deliacte";
        String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken;
        
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #ff9900; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Deliacte</h1>
                        <p>Réinitialisation du mot de passe</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe. Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                        </p>
                        <p>Ce lien expire dans 1 heure.</p>
                        <p>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre mot de passe restera inchangé.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName != null ? firstName : "Utilisateur", resetLink);

        sendEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Bienvenue sur Deliacte !";
        
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #006600; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Bienvenue sur Deliacte !</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre compte a été activé avec succès. Vous pouvez maintenant accéder à tous les services de la plateforme Deliacte.</p>
                        <p>Avec Deliacte, vous pouvez :</p>
                        <ul>
                            <li>Demander des actes administratifs en ligne</li>
                            <li>Suivre l'avancement de vos dossiers</li>
                            <li>Recevoir vos documents directement</li>
                        </ul>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Accéder à mon espace</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName != null ? firstName : "Utilisateur", frontendUrl + "/dashboard");

        sendEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to, String firstName) {
        String subject = "Votre mot de passe a été modifié";
        
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Deliacte</h1>
                        <p>Notification de sécurité</p>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre mot de passe a été modifié avec succès.</p>
                        <div class="warning">
                            <strong>⚠️ Important :</strong> Si vous n'êtes pas à l'origine de cette modification, contactez immédiatement notre support.
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName != null ? firstName : "Utilisateur");

        sendEmail(to, subject, htmlContent);
    }

    @Async
    @Override
    @Transactional  // garantit que la session Hibernate est ouverte pour initialiser les proxies
    public void sendOperationsAssignedEmail(User user, Set<Operation> operations) {

        String subject = "Mise à jour de vos habilitations - Deliacte";



        // Construire le contenu HTML des opérations
        StringBuilder opsHtml = new StringBuilder();
        opsHtml.append("<ul style='list-style: none; padding: 0; margin: 20px 0;'>");

        for (Operation op : operations) {
//            String procedureName = "-";
//            if (op.getProcedure() != null && op.getProcedure().getName() != null) {
//                procedureName = op.getProcedure().getName();
//            }

            opsHtml.append(String.format("""
            <li style='background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 15px; margin-bottom: 10px; display: flex; align-items: center;'>
                <div style='width: 8px; height: 8px; background: #006600; border-radius: 50%%; margin-right: 15px;'></div>
                <div>
                    <div style='font-weight: bold; color: #333;'>%s</div>
                    <div style='font-size: 12px; color: #666;'> Procédure:- </div>
                </div>
            </li>
            """,
                    op.getName() != null ? op.getName() : "-"
//                    procedureName
            ));
        }

        opsHtml.append("</ul>");

        // Contenu complet de l'email
        String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 20px auto; padding: 0; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
                .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 40px 20px; text-align: center; }
                .header h1 { margin: 0; font-size: 28px; letter-spacing: 1px; }
                .content { background: #ffffff; padding: 40px; }
                .welcome-text { font-size: 18px; font-weight: bold; color: #006600; margin-bottom: 15px; }
                .summary-box { background: #f4fbf4; border-radius: 8px; padding: 20px; margin-bottom: 25px; border-left: 4px solid #006600; font-size: 14px; }
                .footer { background: #f9f9f9; text-align: center; padding: 20px; color: #888; font-size: 12px; border-top: 1px solid #eee; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Deliacte</h1>
                    <p>Gestion des Habilitations</p>
                </div>
                <div class="content">
                    <div class="welcome-text">Bonjour %s,</div>
                    <p>Nous vous informons que vos habilitations sur la plateforme <strong>Deliacte</strong> ont été mises à jour. Vous êtes désormais autorisé(e) à effectuer les opérations suivantes :</p>
                    
                    <div class="summary-box">
                        Nombre total d'opérations assignées : <strong>%d</strong>
                    </div>

                    %s

                    <p style="margin-top: 30px; font-size: 14px; color: #666;">Ces modifications sont effectives immédiatement. Vous pouvez vous connecter à votre espace pour commencer à traiter les dossiers correspondants.</p>
                </div>
                <div class="footer">
                    <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                user.getFirstName() != null ? user.getFirstName() : "Utilisateur",
                operations.size(),
                opsHtml.toString()
        );

        // Envoi de l'email
        try {
            sendEmail(user.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'assignation des opérations pour l'utilisateur {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }




    @Override
    @Async
    public void sendDossierSubmittedEmail(String to, String firstName, String dossierNumber, String procedureName) {
        String subject = "Votre demande " + dossierNumber + " a été soumise";
        
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .info-box { background: #e8f5e9; border-left: 4px solid #006600; padding: 15px; margin: 20px 0; }
                    .button { display: inline-block; background: #006600; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Demande soumise</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Votre demande a été soumise avec succès.</p>
                        <div class="info-box">
                            <p><strong>Numéro de dossier :</strong> %s</p>
                            <p><strong>Procédure :</strong> %s</p>
                        </div>
                        <p>Vous pouvez suivre l'avancement de votre demande depuis votre espace personnel.</p>
                        <p style="text-align: center;">
                            <a href="%s/dossiers/%s" class="button">Suivre ma demande</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, dossierNumber, procedureName, frontendUrl, dossierNumber);

        sendEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendDossierStatusChangedEmail(String to, String firstName, String dossierNumber, String newStatus) {
        String subject = "Mise à jour de votre dossier " + dossierNumber;
        
        String statusColor = switch (newStatus.toUpperCase()) {
            case "APPROVED", "COMPLETED" -> "#28a745";
            case "REJECTED" -> "#dc3545";
            case "IN_PROGRESS" -> "#ffc107";
            default -> "#17a2b8";
        };

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .status-badge { display: inline-block; background: %s; color: white; padding: 8px 16px; border-radius: 20px; font-weight: bold; }
                    .button { display: inline-block; background: #006600; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Mise à jour de dossier</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Le statut de votre dossier <strong>%s</strong> a été mis à jour.</p>
                        <p style="text-align: center;">
                            <span class="status-badge">%s</span>
                        </p>
                        <p style="text-align: center;">
                            <a href="%s/dossiers/%s" class="button">Voir les détails</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(statusColor, firstName, dossierNumber, newStatus, frontendUrl, dossierNumber);

        sendEmail(to, subject, htmlContent);
    }

    @Override
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email envoyé avec succès à {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Erreur lors de l'envoi de l'email à {} : {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}
