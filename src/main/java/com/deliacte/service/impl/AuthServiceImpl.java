package com.deliacte.service.impl;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.LoginRequest;
import com.deliacte.dto.request.RegisterRequest;
import com.deliacte.dto.request.UserRequest;
import com.deliacte.dto.response.AuthResponse;
import com.deliacte.dto.response.UserResponse;
import com.deliacte.entity.User;
import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import com.deliacte.exception.BadRequestException;
import com.deliacte.exception.ResourceNotFoundException;
import com.deliacte.repository.UserRepository;
import com.deliacte.security.JwtTokenProvider;
import com.deliacte.service.AuthService;
import com.deliacte.service.EmailService;
import com.deliacte.utils.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        try {
            // Vérifier si l'utilisateur existe
            String email = request.getEmail() == null
                    ? null
                    : request.getEmail().trim();

            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

            // Vérifier si le compte est verrouillé
            if (!user.isAccountNonLocked()) {
                return ApiResponse.error("Votre compte est verrouillé suite à trop de tentatives échouées. Contactez le support.", 403);
            }

            // Vérifier si le compte est activé
            if (!user.isEnabled()) {
                return ApiResponse.error("Votre compte n'est pas encore activé. Vérifiez votre email.", 403);
            }

            // Authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Réinitialiser les tentatives échouées
            user.resetFailedLoginAttempts();
            user.incrementConnectionCount();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Générer les tokens JWT
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            UserResponse userResponse = mapToUserResponse(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                    .user(userResponse)
                    .build();

            log.info("Connexion réussie pour l'utilisateur: {}", user.getEmail());
            return ApiResponse.success(authResponse, "Connexion réussie");

        } catch (BadCredentialsException e) {
            handleFailedLogin(request.getEmail());
            return ApiResponse.error("Email ou mot de passe incorrect", 401);
        } catch (DisabledException e) {
            return ApiResponse.error("Votre compte n'est pas activé", 403);
        } catch (LockedException e) {
            return ApiResponse.error("Votre compte est verrouillé", 403);
        }
    }

    private void handleFailedLogin(String email) {
        userRepository.findByEmailAndDeletedFalse(email).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            if (user.getConnectionFailureCount() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
                log.warn("Compte verrouillé pour l'utilisateur {} après {} tentatives échouées", 
                        email, MAX_FAILED_ATTEMPTS);
            }
            userRepository.save(user);
        });
    }

    @Override
    public ApiResponse<UserResponse> register(UserRequest request) {
        // Cette méthode est pour la compatibilité - utiliser registerWithStatus pour les nouveaux utilisateurs
        return ApiResponse.error("Utilisez la méthode d'inscription avec statut");
    }

    /**
     * Inscription avec gestion des différents statuts (Citoyen, Résident, Réfugié, Diplomate, Étranger)
     */
    @Override
    public ApiResponse<UserResponse> registerWithStatus(RegisterRequest request) {
        // Validation des mots de passe
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.error("Les mots de passe ne correspondent pas");
        }
        String email = request.getEmail() == null
                ? null
                : request.getEmail().trim().toLowerCase();

// Vérifier si l'email existe déjà
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            return ApiResponse.error("Cet email est déjà utilisé");
        }

        // --- Gestion du mot de passe ---
        String rawPassword = request.getPassword();

        // Générer un mot de passe si vide
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = PasswordGenerator.generate(12); // génération de mot de passe rigide
            log.info("Mot de passe généré automatiquement pour l'utilisateur {} : {}", email, rawPassword);
        }


        // Vérifier si le téléphone existe déjà
        if (request.getTelephone() != null && userRepository.existsByTelephoneAndDeletedFalse(request.getTelephone())) {
            return ApiResponse.error("Ce numéro de téléphone est déjà utilisé");
        }

        // Validation des champs requis selon le statut
        if (!request.isValidForStatus()) {
            return ApiResponse.error(request.getMissingFieldsMessage());
        }

        // Vérification spécifique pour les citoyens (ID Unique)
        if (request.getUserStatus() == UserStatus.CITOYEN) {
            if (userRepository.existsByIdUniqueBurkinabeAndDeletedFalse(request.getIdUniqueBurkinabe())) {
                return ApiResponse.error("Cet ID Unique Burkinabè est déjà associé à un compte");
            }
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword)) // encode le mot de passe généré ou fourni
                .telephone(request.getTelephone())
                .userStatus(request.getUserStatus())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .placeOfBirth(request.getPlaceOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .idUniqueBurkinabe(request.getIdUniqueBurkinabe())
                .passportNumber(request.getPassportNumber())
                .residencePermitNumber(request.getResidencePermitNumber())
                .nationality(request.getNationality())
                .refugeeCardNumber(request.getRefugeeCardNumber())
                .diplomaticCardNumber(request.getDiplomaticCardNumber())
                .role(UserRole.CITOYEN)
                .enabled(false)
                .accountNonLocked(true)
                .emailVerified(false)
                .connectionCount(0)
                .connectionFailureCount(0)
                .build();

        // Générer le token de vérification
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        // Envoyer l'email de vérification
        try {
            emailService.sendVerificationEmail(
                    savedUser.getEmail(),
                    savedUser.getFirstName() + " " + savedUser.getLastName(),
                    verificationToken
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de vérification: {}", e.getMessage());
            // On ne bloque pas l'inscription si l'email échoue
        }

        log.info("Nouvel utilisateur inscrit: {} avec statut: {}", savedUser.getEmail(), savedUser.getUserStatus());

        UserResponse response = mapToUserResponse(savedUser);
        return ApiResponse.created(response);
    }

    @Override
    public ApiResponse<AuthResponse> refreshToken(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ApiResponse.error("Token invalide ou expiré", 401);
            }

            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmailAndDeletedFalse(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

            String newAccessToken = jwtTokenProvider.generateAccessToken(user);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

            UserResponse userResponse = mapToUserResponse(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                    .user(userResponse)
                    .build();

            return ApiResponse.success(authResponse, "Token rafraîchi avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            return ApiResponse.error("Token invalide ou expiré", 401);
        }
    }

    @Override
    public ApiResponse<Void> logout(String token) {
        // Dans une implémentation complète, on ajouterait le token à une blacklist
        log.info("Déconnexion effectuée");
        return ApiResponse.success(null, "Déconnexion réussie");
    }

    @Override
    public ApiResponse<Void> forgotPassword(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);

        // Toujours retourner succès pour éviter l'énumération des emails
        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
            }

            log.info("Demande de réinitialisation de mot de passe pour: {}", email);
        }

        return ApiResponse.success(null, "Si cet email existe, un lien de réinitialisation a été envoyé");
    }

    @Override
    public ApiResponse<Void> resetPasswordWithToken(String token, String newPassword) {
        User user = userRepository.findByResetPasswordTokenAndDeletedFalse(token)
                .orElseThrow(() -> new BadRequestException("Token invalide ou expiré"));

        if (user.getResetPasswordTokenExpiry() == null ||
                user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Le lien de réinitialisation a expiré. Veuillez en demander un nouveau.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        // Notifier l'utilisateur
        try {
            emailService.sendPasswordChangedEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification: {}", e.getMessage());
        }

        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());
        return ApiResponse.success(null, "Mot de passe réinitialisé avec succès");
    }

    /**
     * Vérification de l'email avec le token
     */
    public ApiResponse<Void> verifyEmail(String token) {
        User user = userRepository.findByVerificationTokenAndDeletedFalse(token)
                .orElseThrow(() -> new BadRequestException("Token de vérification invalide"));

        if (user.getVerificationTokenExpiry() == null ||
                user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Le lien de vérification a expiré. Veuillez vous réinscrire.");
        }

        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        // Envoyer l'email de bienvenue
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de bienvenue: {}", e.getMessage());
        }

        log.info("Email vérifié pour: {}", user.getEmail());
        return ApiResponse.success(null, "Votre compte a été activé avec succès");
    }

    /**
     * Renvoyer l'email de vérification
     */
    public ApiResponse<Void> resendVerificationEmail(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (user.isEmailVerified()) {
            return ApiResponse.error("Cet email est déjà vérifié");
        }

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
            return ApiResponse.error("Erreur lors de l'envoi de l'email");
        }

        return ApiResponse.success(null, "Email de vérification renvoyé");
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .userStatus(user.getUserStatus())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
