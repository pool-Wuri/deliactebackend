package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.request.LoginRequest;
import com.deliacte.dto.request.RegisterRequest;
import com.deliacte.dto.request.UserRequest;
import com.deliacte.dto.response.AuthResponse;
import com.deliacte.dto.response.UserResponse;

public interface AuthService {

    ApiResponse<AuthResponse> login(LoginRequest request);

    ApiResponse<UserResponse> register(UserRequest request);

    ApiResponse<UserResponse> registerWithStatus(RegisterRequest request);

    ApiResponse<AuthResponse> refreshToken(String refreshToken);

    ApiResponse<Void> logout(String token);

    ApiResponse<Void> forgotPassword(String email);

    ApiResponse<Void> resetPasswordWithToken(String token, String newPassword);

    ApiResponse<Void> verifyEmail(String token);

    ApiResponse<Void> resendVerificationEmail(String email);
}
