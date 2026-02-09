package com.deliacte.config;

import com.deliacte.entity.User;
import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import com.deliacte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {

            List<User> users = List.of(

                    // SUPER ADMIN
                    buildUser(
                            "latioue@gmail.com",
                            "1234",
                            UserRole.SUPER_ADMIN
                    ),

                    // ADMINS D’ORGANISATION
                    buildUser(
                            "latifatou.ouedraogo@tic.gov.bf",
                            "1234",
                            UserRole.RESPONSABLE_ORGANISATION
                    ),
                    buildUser(
                            "jimna.kongo@tic.gov.bf",
                            "1234",
                            UserRole.RESPONSABLE_ORGANISATION
                    ),

                    // CITOYEN
                    buildUser(
                            "yannick.ouedraogo@tic.gov.bf",
                            "1234",
                            UserRole.CITOYEN
                    ),

                    // AGENTS
                    buildUser(
                            "isaac.souli@tic.gov.bf",
                            "1234",
                            UserRole.AGENT
                    ),
                    buildUser(
                            "isaacsouli99@gmail.com",
                            "1234",
                            UserRole.AGENT
                    ),
                    buildUser(
                            "michaelrogerzongo@gmail.com",
                            "1234",
                            UserRole.AGENT
                    ),

                    // MANAGERS DE PROCÉDURE
                    buildUser(
                            "jimnakongo14@gmail.com",
                            "1234",
                            UserRole.ADMIN_PROCEDURE
                    ),
                    buildUser(
                            "zmichaelroger@gmail.com",
                            "1234",
                            UserRole.ADMIN_PROCEDURE
                    )
            );

            users.forEach(user -> {
                userRepository.findByEmail(user.getEmail())
                        .ifPresentOrElse(
                                u -> System.out.println("✔ User already exists: " + u.getEmail()),
                                () -> {
                                    userRepository.save(user);
                                    System.out.println("✅ User created: " + user.getEmail());
                                }
                        );
            });
        };
    }

    private User buildUser(String email, String rawPassword, UserRole role) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .userStatus(UserStatus.CITOYEN)
                .enabled(true)
                .emailVerified(true)
                .accountNonLocked(true)
                .build();
    }
}
