package com.deliacte.repository;

import com.deliacte.entity.User;
import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // === Recherche par email ===
    Optional<User> findByEmailAndDeletedFalse(String email);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByTelephoneAndIdNot(String telephone, UUID id);


    Boolean existsByEmail(String email);

    // === Recherche par téléphone ===
    Optional<User> findByTelephoneAndDeletedFalse(String telephone);
    
    Boolean existsByTelephoneAndDeletedFalse(String telephone);

    // === Recherche par ID Unique Burkinabè ===
    Optional<User> findByIdUniqueBurkinabeAndDeletedFalse(String idUniqueBurkinabe);
    
    Boolean existsByIdUniqueBurkinabeAndDeletedFalse(String idUniqueBurkinabe);

    // === Recherche par tokens ===
    Optional<User> findByVerificationTokenAndDeletedFalse(String token);
    
    Optional<User> findByResetPasswordTokenAndDeletedFalse(String token);

    // === Recherche par ID ===
    Optional<User> findByIdAndDeletedFalse(UUID id);

    // === Listes paginées ===
    Page<User> findByDeletedFalse(Pageable pageable);
    
    Page<User> findByRoleAndDeletedFalse(UserRole role, Pageable pageable);
    
    Page<User> findByUserStatusAndDeletedFalse(UserStatus userStatus, Pageable pageable);
    
    Page<User> findByEnabledAndDeletedFalse(Boolean enabled, Pageable pageable);

    // === Recherche par organisation ===
    @Query("SELECT u FROM User u JOIN u.organisations o WHERE o.id = :organisationId AND u.deleted = false")
    Page<User> findByOrganisationId(@Param("organisationId") UUID organisationId, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.procedures p WHERE p.id = :procedureId AND u.deleted = false")
    List<User> findByProcedureId(@Param("procedureId") UUID procedureId);

    @Query("SELECT u FROM User u JOIN u.operations o WHERE o.id = :operationId AND u.deleted = false")
    List<User> findByOperationId(@Param("operationId") UUID operationId);

    // === Recherche textuelle ===
    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.telephone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.idUniqueBurkinabe) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // === Statistiques ===
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userStatus = :status AND u.deleted = false")
    long countByUserStatus(@Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.deleted = false")
    long countEnabledUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true AND u.deleted = false")
    long countVerifiedUsers();


    /**
     * Récupère les utilisateurs :
     * - qui ont des procédures appartenant à mes organisations
     * - ou que j'ai créés directement
     */
    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN u.procedures p
        LEFT JOIN p.organisation o
        LEFT JOIN o.users orgUser
        WHERE orgUser.id = :myUserId
           OR u.createdBy.id = :myUserId
    """)
    List<User> findUsersLinkedToMyOrganisationsOrCreatedByMe(@Param("myUserId") UUID myUserId);

    @Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN u.procedures p
    LEFT JOIN p.organisation o
    LEFT JOIN o.users orgUser
    WHERE orgUser.id = :myUserId
       OR u.createdBy.id = :myUserId
""")
    Page<User> findUsersLinkedToMyOrganisationsOrCreatedByMe(
            @Param("myUserId") UUID myUserId,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT u
        FROM Operation o
        JOIN o.users u
        WHERE o.id = :operationId
    """)
    List<User> findUsersByOperationId(UUID operationId);

}
