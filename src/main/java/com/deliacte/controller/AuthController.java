package com.deliacte.controller;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.LoginRequest;
import com.deliacte.dto.request.RegisterRequest;
import com.deliacte.dto.request.ResendVerificationRequest;
import com.deliacte.dto.request.ResetPasswordRequest;
import com.deliacte.dto.response.AuthResponse;
import com.deliacte.dto.response.UserResponse;
import com.deliacte.enums.UserStatus;
import com.deliacte.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "API d'authentification et de gestion des comptes")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        ApiResponse<AuthResponse> response = authService.login(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Crée un nouveau compte utilisateur selon le statut (Citoyen, Résident, Réfugié, Diplomate, Étranger)")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<UserResponse> response = authService.registerWithStatus(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rafraîchir le token", description = "Génère un nouveau token d'accès à partir du refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        ApiResponse<AuthResponse> response = authService.refreshToken(refreshToken);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Invalide le token de l'utilisateur")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        ApiResponse<Void> response = authService.logout(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Envoie un email de réinitialisation du mot de passe")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        ApiResponse<Void> response = authService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Réinitialiser le mot de passe",
            description = "Réinitialise le mot de passe avec le token reçu par email"
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {

        ApiResponse<Void> response = authService.resetPasswordWithToken(
                request.getToken(),
                request.getNewPassword()
        );
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/verify")
    @Operation(
            summary = "Vérifier l'email",
            description = "Vérifie l'email de l'utilisateur avec le token reçu"
    )
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        ApiResponse<Void> response = authService.verifyEmail(token);
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }


    @PostMapping("/resend-verification")
    @Operation(
            summary = "Renvoyer l'email de vérification",
            description = "Renvoie l'email de vérification à l'utilisateur"
    )
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestBody @Valid ResendVerificationRequest request) {

        ApiResponse<Void> response = authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/user-statuses")
    @Operation(summary = "Liste des statuts utilisateur", description = "Retourne la liste des statuts disponibles pour l'inscription")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getUserStatuses() {
        List<Map<String, String>> statuses = Arrays.stream(UserStatus.values())
                .map(status -> Map.of(
                        "value", status.name(),
                        "label", status.getLabel(),
                        "description", status.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }
}
