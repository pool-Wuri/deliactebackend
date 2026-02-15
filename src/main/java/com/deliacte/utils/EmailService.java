package com.deliacte.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Injection des valeurs depuis application.yml
    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendAccountCreationEmail(String to, String nom, String prenom, String password) {
        String subject = "Votre compte Deliacte a été créé !";
        String civilite = (nom != null && prenom != null) ? "M./Mme " + nom + " " + prenom : "M./Mme";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; }
                    .container { max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; }
                    .content { background: #fff; padding: 30px; }
                    .info-box { background: #f4fbf4; border-left: 4px solid #006600; padding: 15px; margin: 20px 0; }
                    .button { display: inline-block; background: #007bff; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; }
                    .footer { background: #f9f9f9; text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Deliacte</h1>
                        <p>Plateforme de Services Gouvernementaux</p>
                    </div>
                    <div class="content">
                        <p>%s, votre compte a été créé avec succès </p>
                        <p>Voici vos identifiants :</p>
                        <div class="info-box">
                            Email : <strong>%s</strong><br/>
                            Mot de passe : <strong>%s</strong>
                        </div>
                        <p style="text-align: center;">
                            <a href="%s/login" class="button">Se connecter</a>
                        </p>
                        <p>Merci de changer votre mot de passe dès votre première connexion.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                        <p><a href="%s">%s</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(civilite, to, password, baseUrl, baseUrl, baseUrl);

        sendEmail(to, subject, htmlContent);
    }

    @Async
    public void sendVerificationEmail(String to, String firstName, String verificationToken) {
        String subject = "Vérification de votre compte Deliacte";
        String verificationLink = baseUrl + "/auth/verify?token=" + verificationToken;

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; }
                    .header { background: linear-gradient(135deg, #006600, #004d00); color: white; padding: 30px; text-align: center; }
                    .content { background: #fff; padding: 30px; }
                    .button { display: inline-block; background: #006600; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; }
                    .footer { background: #f9f9f9; text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Deliacte</h1>
                        <p>Plateforme de Services Gouvernementaux</p>
                    </div>
                    <div class="content">
                        <p>Bienvenue %s,</p>
                        <p>Merci de vous être inscrit. Cliquez ci-dessous pour vérifier votre adresse email :</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Vérifier mon compte</a>
                        </p>
                        <p> Ce lien expirera dans 24 heures.</p>
                        <p>Si le bouton ne fonctionne pas, copiez ce lien : <br/><span style="color:#006600;">%s</span></p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName != null ? firstName : "Utilisateur", verificationLink, verificationLink);

        sendEmail(to, subject, htmlContent);
    }



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
                        <strong> Important :</strong> Si vous n'êtes pas à l'origine de cette modification, contactez immédiatement notre support.
                    </div>
                </div>
                <div class="footer">
                    <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName != null ? firstName : "Utilisateur");

        sendEmail(to, subject, htmlContent);
    }



    @Async
    public void sendPasswordResetEmail(String to, String firstName, String resetToken) {
        String subject = "Réinitialisation de votre mot de passe Deliacte";
        String resetLink = baseUrl + "/auth/reset-password?token=" + resetToken;

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
                    <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName != null ? firstName : "Utilisateur", resetLink);

        sendEmail(to, subject, htmlContent);
    }



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
                        <a href="%s/dashboard" class="button">Accéder à mon espace</a>
                    </p>
                </div>
                <div class="footer">
                    <p>© 2024 Deliacte - Burkina Faso<br>Ministère de la Transition Digitale, des Postes et des Communications Électroniques</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName != null ? firstName : "Utilisateur", baseUrl);

        sendEmail(to, subject, htmlContent);
    }





    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Logo inline
            ClassPathResource imgFile = new ClassPathResource("images/LogoOfficiel.png");
            helper.addInline("logoServicePublic", imgFile);

            mailSender.send(message);
            System.err.println("Email envoyé avec succès à " + to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}
