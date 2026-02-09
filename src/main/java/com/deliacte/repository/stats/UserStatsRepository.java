package com.deliacte.repository.stats;
import com.deliacte.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserStatsRepository extends JpaRepository<User, UUID> {

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
    @Query("""
    SELECT COUNT(DISTINCT u)
    FROM User u
    JOIN u.organisations o
    JOIN o.users ou
    WHERE ou.id = :currentUserId
""")
    long countUsersByOrganisationOfUser(@Param("currentUserId") UUID currentUserId);

    @Query("""
    SELECT COUNT(DISTINCT u)
    FROM User u
    JOIN u.procedures p
    JOIN p.users pu
    WHERE pu.id = :currentUserId
""")
    long countUsersByProceduresOfUser(@Param("currentUserId") UUID currentUserId);

    // =============================== // Comptage des utilisateurs par rôle // ===============================
    @Query(" SELECT u.role, COUNT(u) FROM User u GROUP BY u.role ")
    List<Object[]> countUsersGroupedByRole();

}
