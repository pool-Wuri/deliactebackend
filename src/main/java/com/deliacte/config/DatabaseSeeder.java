package com.deliacte.config;

import com.deliacte.entity.*;
import com.deliacte.enums.*;
import com.deliacte.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository              userRepository;
    private final OrganisationRepository      organisationRepository;
    private final ProcedureRepository         procedureRepository;
    private final OperationRepository         operationRepository;
    private final TypeOperationRepository     typeOperationRepository;
    private final EntityObjectRepository      entityObjectRepository;
    private final ChampOperationRepository    champOperationRepository;
    private final OptionChampOperationRepository optionRepo;
    private final ParameterRepository         parameterRepository;
    private final PasswordEncoder             passwordEncoder;

    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void run(String... args) {
        log.info("▶ Démarrage du seeder complet…");

        seedTypeOperations();
        seedEntityObjects();

        Organisation minSecurite  = seedOrganisation("MIN-SECURITE",
                "Ministère de la Sécurité Intérieure",
                "Ministère en charge de la sécurité intérieure et de la protection civile",
                "Ouagadougou", "+226 25 30 64 64", null);

        Organisation dgPasseport  = seedOrganisation("DG-PASSEPORT",
                "Direction Générale des Passeports et Titres de Voyage",
                "Direction en charge de la délivrance des passeports et titres de voyage",
                "Ouagadougou", "+226 25 31 15 00", minSecurite);

        Organisation mairieOuaga  = seedOrganisation("MAIRIE-OUAGA",
                "Mairie de Ouagadougou",
                "Administration communale de la ville de Ouagadougou",
                "Ouagadougou", "+226 25 30 60 61", null);

        Organisation minTic       = seedOrganisation("MIN-TIC",
                "Ministère des Technologies de l'Information et de la Communication",
                "Ministère en charge des TIC et de la transformation numérique",
                "Ouagadougou", "+226 25 33 40 10", null);

        seedOrganisation("AG-SERV",
                "Agence de Services Numériques",
                "Agence en charge de la délivrance des services numériques",
                "Ouagadougou", "+226 25 33 40 20", minTic);

        // ── Users ──────────────────────────────────────────────────────────────
        User superAdmin = seedUser("admin@deliacte.gov.bf", "Admin2024!", UserRole.SUPER_ADMIN,
                "Administrateur", "Système", UserStatus.CITOYEN, null);

        User respPasseport = seedUser("latifatou.ouedraogo@securite.gov.bf", "Agent2024!",
                UserRole.RESPONSABLE_ORGANISATION,
                "Latifatou", "Ouédraogo", UserStatus.CITOYEN, "BF-RESP-001");

        User adminProcPasseport = seedUser("abdoul.diallo@passeport.gov.bf", "Agent2024!",
                UserRole.ADMIN_PROCEDURE,
                "Abdoul", "Diallo", UserStatus.CITOYEN, "BF-ADM-001");

        User agentPasseport = seedUser("agent1.kabore@passeport.gov.bf", "Agent2024!",
                UserRole.AGENT,
                "Issa", "Kaboré", UserStatus.CITOYEN, "BF-AGT-001");

        User respMairie = seedUser("responsable.mairie@mairie-ouaga.bf", "Agent2024!",
                UserRole.RESPONSABLE_ORGANISATION,
                "Nadège", "Somé", UserStatus.CITOYEN, "BF-RESP-002");

        User adminProcMairie = seedUser("nadege.tapsoba@mairie-ouaga.bf", "Agent2024!",
                UserRole.ADMIN_PROCEDURE,
                "Nadège", "Tapsoba", UserStatus.CITOYEN, "BF-ADM-002");

        User agentMairie = seedUser("agent.etatcivil@mairie-ouaga.bf", "Agent2024!",
                UserRole.AGENT,
                "Moussa", "Compaoré", UserStatus.CITOYEN, "BF-AGT-002");

        User citoyen1 = seedUser("kofi.ouedraogo@gmail.com", "Citoyen2024!",
                UserRole.CITOYEN,
                "Kofi", "Ouédraogo", UserStatus.CITOYEN, "BF-CIT-001");

        User citoyen2 = seedUser("aminata.traore@gmail.com", "Citoyen2024!",
                UserRole.CITOYEN,
                "Aminata", "Traoré", UserStatus.CITOYEN, "BF-CIT-002");

        // Anciens comptes (rétrocompatibilité)
        seedUser("latioue@gmail.com",                       "1234", UserRole.SUPER_ADMIN,           "Lati",   "Oue",      UserStatus.CITOYEN, null);
        seedUser("latifatou.ouedraogo@tic.gov.bf",          "1234", UserRole.RESPONSABLE_ORGANISATION,"Latifatou","Ouédraogo",UserStatus.CITOYEN, null);
        seedUser("jimna.kongo@tic.gov.bf",                  "1234", UserRole.ADMIN_PROCEDURE,        "Jimna",  "Kongo",    UserStatus.CITOYEN, null);
        seedUser("yannick.ouedraogo@tic.gov.bf",            "1234", UserRole.CITOYEN,                "Yannick","Ouédraogo",UserStatus.CITOYEN, null);
        seedUser("isaac.souli@tic.gov.bf",                  "1234", UserRole.AGENT,                  "Isaac",  "Souli",    UserStatus.CITOYEN, null);

        // ── Parameters : récupérer les catégories ─────────────────────────────
        Parameter catVoyage = parameterRepository
                .findByTypeAndCode(ParameterType.PROCEDURE_CATEGORY, "TRAVEL").orElse(null);
        Parameter catIdentite = parameterRepository
                .findByTypeAndCode(ParameterType.PROCEDURE_CATEGORY, "IDENTITY").orElse(null);
        Parameter typeMinSecParam = parameterRepository
                .findByTypeAndCode(ParameterType.ORGANISATION_TYPE, "MIN_SECURITY").orElse(null);
        Parameter typeMairieParam = parameterRepository
                .findByTypeAndCode(ParameterType.ORGANISATION_TYPE, "MAIRIE").orElse(null);

        // Mettre à jour le type d'organisation si disponible
        if (typeMinSecParam != null && minSecurite.getOrganisationType() == null) {
            minSecurite.setOrganisationType(typeMinSecParam);
            organisationRepository.save(minSecurite);
        }
        if (typeMairieParam != null && mairieOuaga.getOrganisationType() == null) {
            mairieOuaga.setOrganisationType(typeMairieParam);
            organisationRepository.save(mairieOuaga);
        }

        // ── Procédures ────────────────────────────────────────────────────────
        Procedure procPasseport = seedProcedure(
                "PROC-PASSEPORT",
                "Demande de Passeport Ordinaire",
                "Procédure de demande et de renouvellement du passeport ordinaire burkinabè",
                "Photo d'identité récente, Acte de naissance, CNI ou ancien passeport",
                "21 jours ouvrables",
                new BigDecimal("25000.00"),
                false,
                dgPasseport,
                catVoyage,
                "CITOYEN,RESIDENT"
        );

        Procedure procNaissance = seedProcedure(
                "PROC-NAISSANCE",
                "Demande d'Extrait de Naissance",
                "Procédure de délivrance des extraits de naissance et actes d'état civil",
                "Livret de famille ou preuve de filiation",
                "3 jours ouvrables",
                new BigDecimal("500.00"),
                false,
                mairieOuaga,
                catIdentite,
                "CITOYEN,RESIDENT,REFUGIE,ETRANGER"
        );

        Procedure procCni = seedProcedure(
                "PROC-CNI",
                "Demande de Carte Nationale d'Identité",
                "Procédure de délivrance et de renouvellement de la CNI burkinabè",
                "Acte de naissance, Photo d'identité, Certificat de résidence",
                "10 jours ouvrables",
                new BigDecimal("1000.00"),
                false,
                mairieOuaga,
                catIdentite,
                "CITOYEN"
        );

        // Ancienne procédure visa (rétrocompatibilité)
        seedProcedure(
                "PROC-VISA",
                "Demande de Visa",
                "Procédure de demande de visa pour le Burkina Faso",
                "Passeport valide, Photo d'identité, Invitation ou motif de voyage",
                "7 jours ouvrables",
                new BigDecimal("50.00"),
                true,
                dgPasseport,
                catVoyage,
                "ETRANGER,RESIDENT,DIPLOMATE"
        );

        // ── TypeOperation récupérés ────────────────────────────────────────────
        TypeOperation typesoumission = typeOperationRepository.findByCodeAndDeletedFalse("SOUMISSION").orElse(null);
        TypeOperation typeInstruction = typeOperationRepository.findByCodeAndDeletedFalse("INSTRUCTION").orElse(null);
        TypeOperation typeValidation  = typeOperationRepository.findByCodeAndDeletedFalse("VALIDATION").orElse(null);
        TypeOperation typeCollecte    = typeOperationRepository.findByCodeAndDeletedFalse("COLLECTE").orElse(null);

        // ── Opérations PASSEPORT ──────────────────────────────────────────────
        Operation passOp1 = seedOperation("Soumission de la demande", procPasseport,
                1, true, false, true, typesoumission);
        Operation passOp2 = seedOperation("Instruction du dossier", procPasseport,
                2, false, false, false, typeInstruction);
        Operation passOp3 = seedOperation("Validation administrative", procPasseport,
                3, false, false, false, typeValidation);
        Operation passOp4 = seedOperation("Collecte du passeport", procPasseport,
                4, false, true, true, typeCollecte);

        linkNextPrev(passOp1, passOp2);
        linkNextPrev(passOp2, passOp3);
        linkNextPrev(passOp3, passOp4);

        // ChampOperation pour passOp1
        ChampOperation passNom        = seedChamp("PASS_NOM",       "Nom de famille",            EnumInputFieldType.TEXT,    true,  1, passOp1, null);
        ChampOperation passPrenom     = seedChamp("PASS_PRENOM",    "Prénom(s)",                  EnumInputFieldType.TEXT,    true,  2, passOp1, null);
        ChampOperation passDateNaiss  = seedChamp("PASS_NAISS_DATE","Date de naissance",          EnumInputFieldType.DATE,    true,  3, passOp1, null);
        ChampOperation passLieuNaiss  = seedChamp("PASS_NAISS_LIEU","Lieu de naissance",          EnumInputFieldType.TEXT,    true,  4, passOp1, null);
        ChampOperation passSexe       = seedChamp("PASS_SEXE",      "Sexe",                       EnumInputFieldType.SELECT,  true,  5, passOp1, null);
        ChampOperation passNationalite= seedChamp("PASS_NATIONALITE","Nationalité",               EnumInputFieldType.TEXT,    true,  6, passOp1, null);
        ChampOperation passAdresse    = seedChamp("PASS_ADRESSE",   "Adresse actuelle",           EnumInputFieldType.TEXTAREA,true,  7, passOp1, null);
        ChampOperation passTel        = seedChamp("PASS_TEL",       "Numéro de téléphone",        EnumInputFieldType.PHONE,   true,  8, passOp1, null);
        ChampOperation passEmail      = seedChamp("PASS_EMAIL",     "Adresse email",              EnumInputFieldType.EMAIL,   false, 9, passOp1, null);
        ChampOperation passMotif      = seedChamp("PASS_MOTIF",     "Motif de la demande",        EnumInputFieldType.SELECT,  true,  10, passOp1, null);
        ChampOperation passPhoto      = seedChamp("PASS_PHOTO",     "Photo d'identité (JPEG/PNG)",EnumInputFieldType.IMAGE,   true,  11, passOp1, null);
        ChampOperation passActeNaiss  = seedChamp("PASS_ACTE_NAISS","Acte de naissance (scan PDF)",EnumInputFieldType.FILE,   true,  12, passOp1, null);
        ChampOperation passCni        = seedChamp("PASS_CNI",       "Copie de la CNI (si possession)",EnumInputFieldType.FILE,false,13, passOp1, null);

        seedOption(passSexe,    "Masculin",      "M",              true,  1);
        seedOption(passSexe,    "Féminin",       "F",              false, 2);
        seedOption(passMotif,   "Première demande",    "PREMIERE",   true,  1);
        seedOption(passMotif,   "Renouvellement",      "RENOUVELLEMENT", false, 2);
        seedOption(passMotif,   "Perte",               "PERTE",      false, 3);
        seedOption(passMotif,   "Détérioration",       "DETERIORATION",false,4);

        // ── Opérations NAISSANCE ──────────────────────────────────────────────
        Operation naissOp1 = seedOperation("Soumission de la demande", procNaissance,
                1, true, false, true, typesoumission);
        Operation naissOp2 = seedOperation("Vérification et traitement", procNaissance,
                2, false, false, false, typeInstruction);
        Operation naissOp3 = seedOperation("Approbation et signature", procNaissance,
                3, false, true, false, typeValidation);

        linkNextPrev(naissOp1, naissOp2);
        linkNextPrev(naissOp2, naissOp3);

        ChampOperation naissNom       = seedChamp("NAISS_NOM",       "Nom de la personne concernée", EnumInputFieldType.TEXT,   true, 1, naissOp1, null);
        ChampOperation naissPrenom    = seedChamp("NAISS_PRENOM",    "Prénom(s)",                    EnumInputFieldType.TEXT,   true, 2, naissOp1, null);
        ChampOperation naissDate      = seedChamp("NAISS_DATE",      "Date de naissance",            EnumInputFieldType.DATE,   true, 3, naissOp1, null);
        ChampOperation naissLieu      = seedChamp("NAISS_LIEU",      "Lieu de naissance",            EnumInputFieldType.TEXT,   true, 4, naissOp1, null);
        ChampOperation naissPereNom   = seedChamp("NAISS_PERE_NOM",  "Nom du père",                  EnumInputFieldType.TEXT,   true, 5, naissOp1, null);
        ChampOperation naissPerePrenom= seedChamp("NAISS_PERE_PRENOM","Prénom du père",              EnumInputFieldType.TEXT,   true, 6, naissOp1, null);
        ChampOperation naisssMereNom  = seedChamp("NAISS_MERE_NOM",  "Nom de la mère (nom de jeune fille)",EnumInputFieldType.TEXT,true,7,naissOp1,null);
        ChampOperation naissMerePrenom= seedChamp("NAISS_MERE_PRENOM","Prénom de la mère",           EnumInputFieldType.TEXT,   true, 8, naissOp1, null);
        ChampOperation naissCopies    = seedChamp("NAISS_COPIES",    "Nombre de copies souhaitées",  EnumInputFieldType.NUMBER, true, 9, naissOp1, null);
        ChampOperation naissMotif     = seedChamp("NAISS_MOTIF",     "Motif de la demande",          EnumInputFieldType.SELECT, true, 10, naissOp1, null);
        ChampOperation naissJugement  = seedChamp("NAISS_JUGEMENT",  "Jugement supplétif (si applicable)", EnumInputFieldType.FILE, false, 11, naissOp1, null);

        seedOption(naissMotif, "Usage administratif", "ADMINISTRATIVE", true,  1);
        seedOption(naissMotif, "Usage scolaire",      "SCOLAIRE",       false, 2);
        seedOption(naissMotif, "Mariage",             "MARIAGE",        false, 3);
        seedOption(naissMotif, "Voyage",              "VOYAGE",         false, 4);
        seedOption(naissMotif, "Autre",               "AUTRE",          false, 5);

        // ── Opérations CNI ────────────────────────────────────────────────────
        Operation cniOp1 = seedOperation("Soumission de la demande", procCni,
                1, true, false, true, typesoumission);
        Operation cniOp2 = seedOperation("Instruction du dossier", procCni,
                2, false, false, false, typeInstruction);
        Operation cniOp3 = seedOperation("Validation et édition", procCni,
                3, false, true, false, typeValidation);

        linkNextPrev(cniOp1, cniOp2);
        linkNextPrev(cniOp2, cniOp3);

        ChampOperation cniNom       = seedChamp("CNI_NOM",       "Nom de famille",            EnumInputFieldType.TEXT,    true,  1,  cniOp1, null);
        ChampOperation cniPrenom    = seedChamp("CNI_PRENOM",    "Prénom(s)",                  EnumInputFieldType.TEXT,    true,  2,  cniOp1, null);
        ChampOperation cniDate      = seedChamp("CNI_NAISS_DATE","Date de naissance",          EnumInputFieldType.DATE,    true,  3,  cniOp1, null);
        ChampOperation cniLieu      = seedChamp("CNI_NAISS_LIEU","Lieu de naissance",          EnumInputFieldType.TEXT,    true,  4,  cniOp1, null);
        ChampOperation cniSexe      = seedChamp("CNI_SEXE",      "Sexe",                       EnumInputFieldType.SELECT,  true,  5,  cniOp1, null);
        ChampOperation cniPere      = seedChamp("CNI_PERE",      "Nom et prénom du père",      EnumInputFieldType.TEXT,    true,  6,  cniOp1, null);
        ChampOperation cniMere      = seedChamp("CNI_MERE",      "Nom et prénom de la mère",   EnumInputFieldType.TEXT,    true,  7,  cniOp1, null);
        ChampOperation cniAdresse   = seedChamp("CNI_ADRESSE",   "Adresse de résidence",       EnumInputFieldType.TEXT,    true,  8,  cniOp1, null);
        ChampOperation cniProfession= seedChamp("CNI_PROFESSION","Profession",                 EnumInputFieldType.TEXT,    false, 9,  cniOp1, null);
        ChampOperation cniActeNaiss = seedChamp("CNI_ACTE_NAISS","Acte de naissance (scan PDF)",EnumInputFieldType.FILE,   true,  10, cniOp1, null);
        ChampOperation cniPhoto     = seedChamp("CNI_PHOTO",     "Photo d'identité (JPEG/PNG)",EnumInputFieldType.IMAGE,   true,  11, cniOp1, null);
        ChampOperation cniMotif     = seedChamp("CNI_MOTIF",     "Motif de la demande",        EnumInputFieldType.SELECT,  true,  12, cniOp1, null);

        seedOption(cniSexe,  "Masculin",       "M",               true,  1);
        seedOption(cniSexe,  "Féminin",        "F",               false, 2);
        seedOption(cniMotif, "Première demande","PREMIERE",        true,  1);
        seedOption(cniMotif, "Renouvellement", "RENOUVELLEMENT",   false, 2);
        seedOption(cniMotif, "Perte",          "PERTE",            false, 3);

        // ── Ancienne procédure VISA : opérations rétrocompat ─────────────────
        Procedure procVisa = procedureRepository.findByCode("PROC-VISA").orElse(null);
        if (procVisa != null) {
            Operation visaOp1 = seedOperation("Remplir Formulaire", procVisa, 1, true, false, true, typesoumission);
            Operation visaOp2 = seedOperation("Paiement Visa",      procVisa, 2, false, false, true, typesoumission);
            Operation visaOp3 = seedOperation("Validation Administration", procVisa, 3, false, true, false, typeValidation);
            linkNextPrev(visaOp1, visaOp2);
            linkNextPrev(visaOp2, visaOp3);
        }

        // ── Liens User ↔ Organisation ─────────────────────────────────────────
        linkUserToOrg(respPasseport,    minSecurite);
        linkUserToOrg(respPasseport,    dgPasseport);
        linkUserToOrg(adminProcPasseport, dgPasseport);
        linkUserToOrg(agentPasseport,   dgPasseport);
        linkUserToOrg(respMairie,       mairieOuaga);
        linkUserToOrg(adminProcMairie,  mairieOuaga);
        linkUserToOrg(agentMairie,      mairieOuaga);

        // ── Liens User ↔ Procédure ────────────────────────────────────────────
        linkUserToProc(adminProcPasseport, procPasseport);
        linkUserToProc(adminProcMairie,   procNaissance);
        linkUserToProc(adminProcMairie,   procCni);

        // ── Liens User ↔ Opération (agents) ──────────────────────────────────
        linkUserToOp(agentPasseport, passOp2);
        linkUserToOp(agentMairie,   naissOp2);
        linkUserToOp(agentMairie,   cniOp2);

        log.info("✅ Seeder complet terminé avec succès.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void seedTypeOperations() {
        seedTypeOp("SOUMISSION", "Soumission citoyenne",    "Étape remplie par le citoyen pour initier la demande");
        seedTypeOp("INSTRUCTION","Instruction agent",        "Étape d'instruction et de vérification par un agent");
        seedTypeOp("VALIDATION", "Validation administrative","Étape de validation par un administrateur");
        seedTypeOp("PAIEMENT",   "Paiement des frais",       "Étape de paiement des frais liés à la procédure");
        seedTypeOp("COLLECTE",   "Collecte du document",     "Étape de retrait du document par le citoyen");
    }

    private void seedTypeOp(String code, String libelle, String description) {
        typeOperationRepository.findByCodeAndDeletedFalse(code).ifPresentOrElse(
                t -> log.debug("✔ TypeOperation existe: {}", code),
                () -> {
                    typeOperationRepository.save(TypeOperation.builder()
                            .code(code).libelle(libelle).description(description).active(true).build());
                    log.info("✅ TypeOperation créé: {}", code);
                });
    }

    private void seedEntityObjects() {
        seedEntityObject("DEMANDEUR",     "Demandeur",          "Informations sur la personne qui fait la demande");
        seedEntityObject("REPRESENTANT",  "Représentant légal", "Informations sur le tuteur ou représentant légal");
        seedEntityObject("DOC_FOURNI",    "Document fourni",    "Documents justificatifs à joindre au dossier");
    }

    private void seedEntityObject(String code, String name, String description) {
        entityObjectRepository.findByCodeAndDeletedFalse(code).ifPresentOrElse(
                e -> log.debug("✔ EntityObject existe: {}", code),
                () -> {
                    entityObjectRepository.save(EntityObject.builder()
                            .code(code).name(name).description(description).isActive(true).build());
                    log.info("✅ EntityObject créé: {}", code);
                });
    }

    private Organisation seedOrganisation(String code, String name, String description,
                                          String address, String telephone, Organisation parent) {
        return organisationRepository.findByCode(code).orElseGet(() -> {
            Organisation org = Organisation.builder()
                    .code(code).name(name).description(description)
                    .address(address).telephone(telephone)
                    .parent(parent).isActive(true).build();
            organisationRepository.save(org);
            log.info("✅ Organisation créée: {}", code);
            return org;
        });
    }

    private User seedUser(String email, String rawPassword, UserRole role,
                          String firstName, String lastName,
                          UserStatus status, String idUnique) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .firstName(firstName)
                    .lastName(lastName)
                    .userStatus(status)
                    .idUniqueBurkinabe(idUnique)
                    .enabled(true)
                    .emailVerified(true)
                    .accountNonLocked(true)
                    .build();
            userRepository.save(u);
            log.info("✅ Utilisateur créé: {} ({})", email, role);
            return u;
        });
    }

    private Procedure seedProcedure(String code, String name, String description,
                                    String requirements, String processingTime,
                                    BigDecimal fee, boolean hasPayment,
                                    Organisation organisation, Parameter category,
                                    String allowedUserStatuses) {
        return procedureRepository.findByCode(code).orElseGet(() -> {
            Procedure p = Procedure.builder()
                    .code(code).name(name).description(description)
                    .requirements(requirements).processingTime(processingTime)
                    .fee(fee).hasPayment(hasPayment)
                    .organisation(organisation).category(category)
                    .allowedUserStatuses(allowedUserStatuses)
                    .status(ProcedureStatus.PUBLISHED).isPublic(true)
                    .build();
            procedureRepository.save(p);
            log.info("✅ Procédure créée: {}", code);
            return p;
        });
    }

    private Operation seedOperation(String name, Procedure procedure, int orderIndex,
                                    boolean isCitizen, boolean isLast, boolean isFirst,
                                    TypeOperation typeOperation) {
        return operationRepository.findByNameAndProcedure(name, procedure).orElseGet(() -> {
            Operation op = Operation.builder()
                    .name(name).procedure(procedure)
                    .orderIndex(orderIndex)
                    .isCitizenOperation(isCitizen)
                    .isFirstOperation(isFirst)
                    .isLastOperation(isLast)
                    .isActive(true)
                    .typeOperation(typeOperation)
                    .build();
            operationRepository.save(op);
            log.info("✅ Opération créée: '{}' pour {}", name, procedure.getCode());
            return op;
        });
    }

    private void linkNextPrev(Operation from, Operation to) {
        // Recharger les entités dans la session courante
        Operation f = operationRepository.findById(from.getId()).orElse(null);
        Operation t = operationRepository.findById(to.getId()).orElse(null);
        if (f == null || t == null) return;

        boolean changed = false;
        if (!f.getNextOperations().contains(t)) {
            f.getNextOperations().add(t);
            changed = true;
        }
        if (!t.getPreviousOperations().contains(f)) {
            t.getPreviousOperations().add(f);
            changed = true;
        }
        if (changed) {
            operationRepository.save(f);
            operationRepository.save(t);
            log.info("✅ Lien opération: '{}' → '{}'", f.getName(), t.getName());
        }
    }

    private ChampOperation seedChamp(String code, String label, EnumInputFieldType fieldType,
                                     boolean required, int order, Operation operation,
                                     EntityObject entityObject) {
        return champOperationRepository.findByLabelAndOperation(label, operation).orElseGet(() -> {
            ChampOperation c = ChampOperation.builder()
                    .code(code).label(label).fieldType(fieldType)
                    .required(required).order(order).orderIndex(order)
                    .active(true).operation(operation).entityObject(entityObject)
                    .build();
            champOperationRepository.save(c);
            log.info("✅ Champ créé: '{}' ({})", label, fieldType);
            return c;
        });
    }

    private void seedOption(ChampOperation champ, String label, String value,
                            boolean isDefault, int orderIndex) {
        optionRepo.findByValueAndChampOperation(value, champ).ifPresentOrElse(
                o -> log.debug("✔ Option existe: {}", value),
                () -> {
                    optionRepo.save(OptionChampOperation.builder()
                            .label(label).value(value)
                            .isDefault(isDefault).orderIndex(orderIndex)
                            .champOperation(champ).build());
                    log.info("✅ Option créée: '{}' pour {}", label, champ.getCode());
                });
    }

    private void linkUserToOrg(User user, Organisation org) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if (u == null) return;
        if (!u.getOrganisations().contains(org)) {
            u.getOrganisations().add(org);
            userRepository.save(u);
            log.info("✅ Lien User↔Org: {} → {}", u.getEmail(), org.getCode());
        }
    }

    private void linkUserToProc(User user, Procedure proc) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if (u == null) return;
        if (!u.getProcedures().contains(proc)) {
            u.getProcedures().add(proc);
            userRepository.save(u);
            log.info("✅ Lien User↔Proc: {} → {}", u.getEmail(), proc.getCode());
        }
    }

    private void linkUserToOp(User user, Operation op) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if (u == null) return;
        if (!u.getOperations().contains(op)) {
            u.getOperations().add(op);
            userRepository.save(u);
            log.info("✅ Lien User↔Op: {} → '{}'", u.getEmail(), op.getName());
        }
    }
}
