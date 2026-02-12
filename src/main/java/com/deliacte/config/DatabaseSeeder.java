package com.deliacte.config;

import com.deliacte.entity.*;
import com.deliacte.enums.*;
import com.deliacte.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final ProcedureRepository procedureRepository;
    private final OperationRepository operationRepository;
    private final ChampOperationRepository champOperationRepository;
    private final OptionChampOperationRepository optionChampOperationRepository;
    private final PasswordEncoder passwordEncoder;
//
//    @Bean
//    CommandLineRunner seedDatabase() {
//        return args -> {
//
//            // -------------------------
//            // 1️⃣ Seed Users
//            // -------------------------
//            List<User> users = List.of(
//                    buildUser("latioue@gmail.com", "1234", UserRole.SUPER_ADMIN),
//                    buildUser("latifatou.ouedraogo@tic.gov.bf", "1234", UserRole.RESPONSABLE_ORGANISATION),
//                    buildUser("jimna.kongo@tic.gov.bf", "1234", UserRole.RESPONSABLE_ORGANISATION),
//                    buildUser("yannick.ouedraogo@tic.gov.bf", "1234", UserRole.CITOYEN),
//                    buildUser("isaac.souli@tic.gov.bf", "1234", UserRole.AGENT)
//            );
//
//            users.forEach(user -> userRepository.findByEmail(user.getEmail())
//                    .ifPresentOrElse(
//                            u -> System.out.println("✔ User exists: " + u.getEmail()),
//                            () -> {
//                                userRepository.save(user);
//                                System.out.println("✅ User created: " + user.getEmail());
//                            }
//                    ));
//
//            // -------------------------
//            // 2️⃣ Seed Organisations
//            // -------------------------
//            Organisation ministry = Organisation.builder()
//                    .name("Ministère TIC")
//                    .code("MIN-TIC")
//                    .description("Ministère en charge des TIC")
//                    .isActive(true)
//                    .build();
//            ministry = organisationRepository.save(ministry);
//
//            Organisation agency = Organisation.builder()
//                    .name("Agence de Services")
//                    .code("AG-SERV")
//                    .description("Agence en charge de la délivrance des documents")
//                    .parent(ministry)
//                    .isActive(true)
//                    .build();
//            agency = organisationRepository.save(agency);
//
//            // -------------------------
//            // 3️⃣ Seed Procedures
//            // -------------------------
//            Procedure visaProcedure = Procedure.builder()
//                    .name("Demande de Visa")
//                    .code("PROC-VISA")
//                    .description("Procédure de demande de visa pour Burkina Faso")
//                    .organisation(agency)
//                    .fee(new BigDecimal("50.00"))
//                    .status(ProcedureStatus.PUBLISHED)
//                    .isPublic(true)
//                    .hasPayment(true)
//                    .build();
//            visaProcedure = procedureRepository.save(visaProcedure);
//
//            // -------------------------
//            // 4️⃣ Seed Operations
//            // -------------------------
//            Operation op1 = Operation.builder()
//                    .name("Remplir Formulaire")
//                    .orderIndex(1)
//                    .hasPayment(false)
//                    .isCitizenOperation(true)
//                    .procedure(visaProcedure)
//                    .build();
//            op1 = operationRepository.save(op1);
//
//            Operation op2 = Operation.builder()
//                    .name("Paiement Visa")
//                    .orderIndex(2)
//                    .hasPayment(true)
//                    .isCitizenOperation(true)
//                    .procedure(visaProcedure)
//                    .build();
//            op2 = operationRepository.save(op2);
//
//            Operation op3 = Operation.builder()
//                    .name("Validation Administration")
//                    .orderIndex(3)
//                    .hasPayment(false)
//                    .isCitizenOperation(false)
//                    .procedure(visaProcedure)
//                    .build();
//            op3 = operationRepository.save(op3);
//
//            // Liaison des opérations pour previous/next
//            op1.getNextOperations().add(op2);
//            op2.getPreviousOperations().add(op1);
//            op2.getNextOperations().add(op3);
//            op3.getPreviousOperations().add(op2);
//
//            operationRepository.save(op1);
//            operationRepository.save(op2);
//            operationRepository.save(op3);
//
//            // -------------------------
//            // 5️⃣ Seed ChampOperations
//            // -------------------------
//            ChampOperation champNom = ChampOperation.builder()
//                    .label("Nom complet")
//                    .fieldType(EnumInputFieldType.TEXT)
//                    .required(true)
//                    .order(1)
//                    .operation(op1)
//                    .build();
//            champNom = champOperationRepository.save(champNom);
//
//            ChampOperation champPasseport = ChampOperation.builder()
//                    .label("Numéro Passeport")
//                    .fieldType(EnumInputFieldType.TEXT)
//                    .required(true)
//                    .order(2)
//                    .operation(op1)
//                    .build();
//            champPasseport = champOperationRepository.save(champPasseport);
//
//            // -------------------------
//            // 6️⃣ Seed OptionChampOperations
//            // -------------------------
//            OptionChampOperation optionBurkina = OptionChampOperation.builder()
//                    .label("Burkina Faso")
//                    .value("BF")
//                    .isDefault(true)
//                    .champOperation(champNom)
//                    .build();
//            optionChampOperationRepository.save(optionBurkina);
//
//            OptionChampOperation optionCoteIvoire = OptionChampOperation.builder()
//                    .label("Côte d'Ivoire")
//                    .value("CI")
//                    .champOperation(champNom)
//                    .build();
//            optionChampOperationRepository.save(optionCoteIvoire);
//
//            System.out.println("✅ Database seeding completed successfully!");
//        };
//    }

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
