package com.deliacte.service;

import com.deliacte.dto.ApiResponse;
import com.deliacte.dto.PageResponse;
import com.deliacte.dto.request.ChangePasswordRequest;
import com.deliacte.dto.request.IdListRequest;
import com.deliacte.dto.request.UserRequest;
import com.deliacte.dto.response.UserResponse;
import com.deliacte.enums.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserService {

    ApiResponse<UserResponse> createUser(UserRequest request);

    ApiResponse<UserResponse> updateUser(UUID id, UserRequest request);

    ApiResponse<UserResponse> getUserById(UUID id);

    ApiResponse<UserResponse> getUserByEmail(String email);

    ApiResponse<PageResponse<UserResponse>> getAllUsers(Pageable pageable);

    ApiResponse<PageResponse<UserResponse>> getUsersByRole(UserRole role, Pageable pageable);

    ApiResponse<PageResponse<UserResponse>> getUsersByOrganisation(UUID organisationId, Pageable pageable);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<UserResponse>> getUsersByProcedure(
            UUID procedureId,
            Pageable pageable   // conservé pour cohérence API
    );

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<UserResponse>> getUsersByOperation(
            UUID operationId,
            Pageable pageable
    );

    ApiResponse<PageResponse<UserResponse>> searchUsers(String search, Pageable pageable);

    ApiResponse<Void> deleteUser(UUID id);

    ApiResponse<UserResponse> activateUser(UUID id);

    ApiResponse<UserResponse> deactivateUser(UUID id);

    ApiResponse<Void> changePassword(UUID id, String oldPassword, String newPassword);

    ApiResponse<Void> resetPassword(UUID id, String newPassword);

    ApiResponse<UserResponse> assignOrganisations(UUID userId, IdListRequest ids);

    ApiResponse<UserResponse> assignProcedures(UUID userId, IdListRequest ids);

    ApiResponse<UserResponse> assignOperations(UUID userId,   IdListRequest ids);
    ApiResponse<Void> changePassword(UUID userId, ChangePasswordRequest request);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<UserResponse>> getUsersOrCreatedByMe(Pageable pageable);

    @Transactional(readOnly = true)
    ApiResponse<PageResponse<UserResponse>> findByOperationId(UUID operationId);
}
