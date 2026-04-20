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

/**
 * Seeder de procédures complexes illustrant trois cas avancés :
 *
 *  1. PROC-PERMIS-CONSTRUIRE — Permis de construire
 *     Démontre :
 *       • Étapes citoyennes intermédiaires (pas seulement en première position)
 *       • Branchement multiple (une opération → plusieurs suivantes en parallèle)
 *       • Convergence multiple (plusieurs précédentes → une opération de synthèse)
 *
 *     Graphe :
 *
 *       [C] Op1 ─ Dépôt du dossier
 *            │
 *            ├──→ [A] Op2a ─ Étude technique
 *            └──→ [A] Op2b ─ Étude foncière
 *                     │             │
 *                     └──────┬──────┘   ← convergence (Op2a ET Op2b → Op3)
 *                            ↓
 *                       [C] Op3 ─ Compléments du demandeur  (étape citoyenne intermédiaire)
 *                            ↓
 *                       [A] Op4 ─ Validation finale
 *                            ↓
 *                       [C] Op5 ─ Retrait du permis  (étape citoyenne finale)
 *
 *  2. PROC-BOURSE-ETUDES — Bourse d'études supérieures
 *     Démontre :
 *       • Étape citoyenne en position médiane (compléments dossier)
 *       • Branchement vers instruction thématique double (académique + social)
 *       • Convergence des deux branches vers la décision
 *
 *     Graphe :
 *
 *       [C] Op1 ─ Dépôt candidature
 *            ↓
 *       [A] Op2 ─ Vérification admissibilité
 *            ↓
 *       [C] Op3 ─ Compléments étudiant  (étape citoyenne intermédiaire)
 *            │
 *            ├──→ [A] Op4a ─ Évaluation académique
 *            └──→ [A] Op4b ─ Évaluation situation sociale
 *                     │             │
 *                     └──────┬──────┘   ← convergence
 *                            ↓
 *                       [A] Op5 ─ Délibération commission
 *                            ↓
 *                       [C] Op6 ─ Acceptation / Signature  (étape citoyenne finale)
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class ProcedureComplexeSeeder implements CommandLineRunner {

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

    // ══════════════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void run(String... args) {
        log.info("▶ ProcedureComplexeSeeder — démarrage…");

        seedExtraEntityObjects();
        seedExtraTypeOperations();

        // ── Organisations ─────────────────────────────────────────────────────
        Organisation minUrban = seedOrg("MIN-URBAN",
                "Ministère de l'Urbanisme et de l'Habitat",
                "Ministère en charge de l'urbanisme, de la construction et de l'habitat",
                "Ouagadougou", "+226 25 36 10 00", null);

        Organisation dgUrban = seedOrg("DG-URBAN",
                "Direction Générale de l'Urbanisme",
                "Direction en charge de la délivrance des permis de construire",
                "Ouagadougou", "+226 25 36 10 50", minUrban);

        Organisation minEdu = seedOrg("MIN-EDU",
                "Ministère de l'Éducation Nationale",
                "Ministère en charge de l'éducation et des bourses d'études",
                "Ouagadougou", "+226 25 30 66 00", null);

        Organisation dgBourses = seedOrg("DG-BOURSES",
                "Direction Générale des Bourses",
                "Direction en charge de l'attribution des bourses d'études supérieures",
                "Ouagadougou", "+226 25 30 67 00", minEdu);

        // ── Agents spécialisés ────────────────────────────────────────────────
        User agentTechnique   = seedUser("agent.technique@urbanisme.gov.bf",   "Agent2024!", UserRole.AGENT,
                "Issouf",  "Zongo",     "BF-AGT-TECH-01");
        User agentFoncier     = seedUser("agent.foncier@urbanisme.gov.bf",     "Agent2024!", UserRole.AGENT,
                "Rasmata", "Kaboré",    "BF-AGT-FONC-01");
        User agentValidateur  = seedUser("agent.validateur@urbanisme.gov.bf",  "Agent2024!", UserRole.AGENT,
                "Hamidou", "Sawadogo",  "BF-AGT-VALID-01");
        User agentAdmissib    = seedUser("agent.admissib@bourses.gov.bf",      "Agent2024!", UserRole.AGENT,
                "Fatimata","Ouédraogo", "BF-AGT-ADM-01");
        User agentAcademique  = seedUser("agent.academique@bourses.gov.bf",    "Agent2024!", UserRole.AGENT,
                "Alassane","Traoré",    "BF-AGT-ACA-01");
        User agentSocial      = seedUser("agent.social@bourses.gov.bf",        "Agent2024!", UserRole.AGENT,
                "Bintou",  "Diallo",    "BF-AGT-SOC-01");
        User agentCommission  = seedUser("commission.bourses@bourses.gov.bf",  "Agent2024!", UserRole.AGENT,
                "Mahamadi","Compaoré",  "BF-AGT-COM-01");
        User adminUrban       = seedUser("admin.proc@urbanisme.gov.bf",        "Agent2024!", UserRole.ADMIN_PROCEDURE,
                "Seydou",  "Ouédraogo", "BF-ADM-URB-01");
        User adminEdu         = seedUser("admin.proc@bourses.gov.bf",          "Agent2024!", UserRole.ADMIN_PROCEDURE,
                "Clarisse","Somé",      "BF-ADM-EDU-01");

        // ── Catégories ────────────────────────────────────────────────────────
        Parameter catProperty = parameterRepository
                .findByTypeAndCode(ParameterType.PROCEDURE_CATEGORY, "PROPERTY").orElse(null);
        Parameter catEducation = parameterRepository
                .findByTypeAndCode(ParameterType.PROCEDURE_CATEGORY, "EDUCATION").orElse(null);

        // ══════════════════════════════════════════════════════════════════════
        //  PROCÉDURE 1 : PERMIS DE CONSTRUIRE
        // ══════════════════════════════════════════════════════════════════════
        Procedure procPermis = seedProc(
                "PROC-PERMIS-CONSTRUIRE",
                "Demande de Permis de Construire",
                "Procédure de demande et d'obtention du permis de construire pour toute construction neuve ou rénovation lourde sur le territoire national.",
                "Plan de masse signé par un architecte agréé; Titre foncier ou contrat de bail; Photo de la parcelle; CNI du demandeur; Devis estimatif",
                "45 jours ouvrables",
                new BigDecimal("75000.00"),
                true,
                dgUrban,
                catProperty,
                "CITOYEN,RESIDENT,ETRANGER"
        );

        // ── Types opération récupérés ─────────────────────────────────────────
        TypeOperation typeSoumission  = typeOperationRepository.findByCodeAndDeletedFalse("SOUMISSION").orElse(null);
        TypeOperation typeInstruction = typeOperationRepository.findByCodeAndDeletedFalse("INSTRUCTION").orElse(null);
        TypeOperation typeValidation  = typeOperationRepository.findByCodeAndDeletedFalse("VALIDATION").orElse(null);
        TypeOperation typeCollecte    = typeOperationRepository.findByCodeAndDeletedFalse("COLLECTE").orElse(null);
        TypeOperation typeComplement  = typeOperationRepository.findByCodeAndDeletedFalse("COMPLEMENT").orElse(null);
        TypeOperation typeEvaluation  = typeOperationRepository.findByCodeAndDeletedFalse("EVALUATION").orElse(null);
        TypeOperation typeDeliberation= typeOperationRepository.findByCodeAndDeletedFalse("DELIBERATION").orElse(null);

        // ─────────────────────────────────────────────────────────────────────
        //  OPÉRATIONS PERMIS DE CONSTRUIRE
        //  Op1 [C,first] ──→ Op2a [A]  (branche technique)
        //                ──→ Op2b [A]  (branche foncière)   ← multiple suivants
        //  Op2a + Op2b   ──→ Op3 [C]                        ← multiple précédents
        //  Op3           ──→ Op4 [A]
        //  Op4           ──→ Op5 [C,last]
        // ─────────────────────────────────────────────────────────────────────

        Operation permisOp1 = seedOp(
                "Dépôt du dossier de construction",
                procPermis, 1,
                true,   // isCitizenOperation
                false,  // isLastOperation
                true,   // isFirstOperation
                typeSoumission
        );

        Operation permisOp2a = seedOp(
                "Étude technique architecturale",
                procPermis, 2,
                false, false, false,
                typeInstruction
        );

        Operation permisOp2b = seedOp(
                "Étude foncière et domaniale",
                procPermis, 2,   // même orderIndex : exécution en parallèle
                false, false, false,
                typeInstruction
        );

        Operation permisOp3 = seedOp(
                "Compléments du demandeur",
                procPermis, 3,
                true,   // isCitizenOperation — ÉTAPE CITOYENNE INTERMÉDIAIRE
                false, false,
                typeComplement
        );

        Operation permisOp4 = seedOp(
                "Validation et signature du permis",
                procPermis, 4,
                false, false, false,
                typeValidation
        );

        Operation permisOp5 = seedOp(
                "Retrait du permis de construire",
                procPermis, 5,
                true,  // isCitizenOperation — ÉTAPE CITOYENNE FINALE
                true,  // isLastOperation
                false,
                typeCollecte
        );

        // Liens : Op1 → Op2a ET Op2b  (branchement multiple)
        linkNextPrev(permisOp1, permisOp2a);
        linkNextPrev(permisOp1, permisOp2b);

        // Liens : Op2a → Op3  ET  Op2b → Op3  (convergence multiple)
        linkNextPrev(permisOp2a, permisOp3);
        linkNextPrev(permisOp2b, permisOp3);

        // Chaîne linéaire après la convergence
        linkNextPrev(permisOp3, permisOp4);
        linkNextPrev(permisOp4, permisOp5);

        // ── Champs Op1 — Dépôt (citoyen, premier) ────────────────────────────
        EntityObject entDemandeur = entityObjectRepository.findByCodeAndDeletedFalse("DEMANDEUR").orElse(null);
        EntityObject entDoc       = entityObjectRepository.findByCodeAndDeletedFalse("DOC_FOURNI").orElse(null);
        EntityObject entTerrain   = entityObjectRepository.findByCodeAndDeletedFalse("TERRAIN").orElse(null);

        ChampOperation pNom       = seedChamp("PC_NOM",        "Nom de famille du demandeur",     EnumInputFieldType.TEXT,     true,  1,  permisOp1, entDemandeur);
        ChampOperation pPrenom    = seedChamp("PC_PRENOM",     "Prénom(s) du demandeur",          EnumInputFieldType.TEXT,     true,  2,  permisOp1, entDemandeur);
        ChampOperation pCni       = seedChamp("PC_CNI",        "Numéro de la CNI",                EnumInputFieldType.TEXT,     true,  3,  permisOp1, entDemandeur);
        ChampOperation pTel       = seedChamp("PC_TEL",        "Téléphone",                       EnumInputFieldType.PHONE,    true,  4,  permisOp1, entDemandeur);
        ChampOperation pEmail     = seedChamp("PC_EMAIL",      "Email",                           EnumInputFieldType.EMAIL,    false, 5,  permisOp1, entDemandeur);
        ChampOperation pAdresse   = seedChamp("PC_ADRESSE",    "Adresse complète de résidence",   EnumInputFieldType.TEXTAREA, true,  6,  permisOp1, entDemandeur);

        ChampOperation pLocTerrain= seedChamp("PC_LOC_TERRAIN","Localisation du terrain",         EnumInputFieldType.TEXT,     true,  7,  permisOp1, entTerrain);
        ChampOperation pSuperficie= seedChamp("PC_SUPERFICIE", "Superficie du terrain (m²)",      EnumInputFieldType.NUMBER,   true,  8,  permisOp1, entTerrain);
        ChampOperation pTypeConstr= seedChamp("PC_TYPE_CONSTR","Type de construction",             EnumInputFieldType.SELECT,   true,  9,  permisOp1, entTerrain);
        ChampOperation pNiveaux   = seedChamp("PC_NIVEAUX",    "Nombre de niveaux prévus",        EnumInputFieldType.NUMBER,   true,  10, permisOp1, entTerrain);
        ChampOperation pSurface   = seedChamp("PC_SURFACE_B",  "Surface bâtie prévue (m²)",       EnumInputFieldType.NUMBER,   true,  11, permisOp1, entTerrain);
        ChampOperation pUsage     = seedChamp("PC_USAGE",      "Usage de la construction",        EnumInputFieldType.SELECT,   true,  12, permisOp1, entTerrain);

        ChampOperation pPlan      = seedChamp("PC_PLAN",       "Plan de masse signé architecte (PDF)", EnumInputFieldType.FILE,  true,  13, permisOp1, entDoc);
        ChampOperation pTitreFonc = seedChamp("PC_TITRE_FONC", "Titre foncier ou contrat de bail (PDF)", EnumInputFieldType.FILE, true, 14, permisOp1, entDoc);
        ChampOperation pPhotoParcelle = seedChamp("PC_PHOTO_PARC","Photo de la parcelle (JPG/PNG)", EnumInputFieldType.IMAGE,  true,  15, permisOp1, entDoc);
        ChampOperation pDevis     = seedChamp("PC_DEVIS",      "Devis estimatif des travaux (PDF)", EnumInputFieldType.FILE,   true,  16, permisOp1, entDoc);
        ChampOperation pArchi     = seedChamp("PC_ARCHI_AGREE","Architecte agréé responsable",   EnumInputFieldType.TEXT,     true,  17, permisOp1, null);
        ChampOperation pMotif     = seedChamp("PC_MOTIF",      "Objet de la demande",             EnumInputFieldType.SELECT,   true,  18, permisOp1, null);

        seedOption(pTypeConstr, "Résidentiel individuel",   "RESIDENTIEL",   true,  1);
        seedOption(pTypeConstr, "Résidentiel collectif",    "RES_COLLECTIF", false, 2);
        seedOption(pTypeConstr, "Commercial",               "COMMERCIAL",    false, 3);
        seedOption(pTypeConstr, "Industriel",               "INDUSTRIEL",    false, 4);
        seedOption(pTypeConstr, "Équipement public",        "EQUIPEMENT",    false, 5);

        seedOption(pUsage,      "Habitation principale",    "HABITATION",    true,  1);
        seedOption(pUsage,      "Location",                 "LOCATION",      false, 2);
        seedOption(pUsage,      "Commerce",                 "COMMERCE",      false, 3);
        seedOption(pUsage,      "Bureaux",                  "BUREAUX",       false, 4);

        seedOption(pMotif,      "Construction neuve",       "NEUVE",         true,  1);
        seedOption(pMotif,      "Extension",                "EXTENSION",     false, 2);
        seedOption(pMotif,      "Rénovation lourde",        "RENOVATION",    false, 3);
        seedOption(pMotif,      "Reconstruction",           "RECONSTRUCTION",false, 4);

        // ── Champs Op2a — Étude technique (agent) ─────────────────────────────
        ChampOperation pConfArchi = seedChamp("PC_CONF_ARCHI",  "Conformité architecturale",       EnumInputFieldType.SELECT,   true,  1, permisOp2a, null);
        ChampOperation pContraintTech= seedChamp("PC_CONTRAINTES","Contraintes techniques détectées",EnumInputFieldType.TEXTAREA, false, 2, permisOp2a, null);
        ChampOperation pRapTech   = seedChamp("PC_RAP_TECH",    "Rapport d'étude technique (PDF)", EnumInputFieldType.FILE,     true,  3, permisOp2a, null);
        ChampOperation pObsTech   = seedChamp("PC_OBS_TECH",    "Observations techniques",         EnumInputFieldType.TEXTAREA, false, 4, permisOp2a, null);
        ChampOperation pNoteTech  = seedChamp("PC_NOTE_TECH",   "Note technique globale (/20)",    EnumInputFieldType.NUMBER,   true,  5, permisOp2a, null);

        seedOption(pConfArchi, "Conforme aux normes",         "CONFORME",      true,  1);
        seedOption(pConfArchi, "Conforme avec réserves",      "AVEC_RESERVES", false, 2);
        seedOption(pConfArchi, "Non conforme",                "NON_CONFORME",  false, 3);

        // ── Champs Op2b — Étude foncière (agent) ──────────────────────────────
        ChampOperation pStatutFonc= seedChamp("PC_STATUT_FONC", "Statut foncier de la parcelle",  EnumInputFieldType.SELECT,   true,  1, permisOp2b, null);
        ChampOperation pNumParc   = seedChamp("PC_NUM_PARC",    "Numéro de parcelle cadastrale",  EnumInputFieldType.TEXT,     true,  2, permisOp2b, null);
        ChampOperation pRapFonc   = seedChamp("PC_RAP_FONC",    "Rapport foncier (PDF)",          EnumInputFieldType.FILE,     true,  3, permisOp2b, null);
        ChampOperation pObsFonc   = seedChamp("PC_OBS_FONC",    "Observations foncières",         EnumInputFieldType.TEXTAREA, false, 4, permisOp2b, null);
        ChampOperation pConflits  = seedChamp("PC_CONFLITS",    "Litiges fonciers en cours",      EnumInputFieldType.SELECT,   true,  5, permisOp2b, null);

        seedOption(pStatutFonc, "Titre foncier enregistré", "TF_ENREGISTRE", true,  1);
        seedOption(pStatutFonc, "Contrat de bail régulier", "BAIL_REGULIER", false, 2);
        seedOption(pStatutFonc, "Lotissement non titré",    "LOT_NON_TITRE", false, 3);
        seedOption(pStatutFonc, "Occupation précaire",      "PRECAIRE",      false, 4);

        seedOption(pConflits,   "Aucun",                    "AUCUN",         true,  1);
        seedOption(pConflits,   "Litige en cours",          "LITIGE",        false, 2);
        seedOption(pConflits,   "Résolu",                   "RESOLU",        false, 3);

        // ── Champs Op3 — Compléments demandeur (citoyen, intermédiaire) ───────
        // Le citoyen reçoit les observations des deux branches et complète son dossier
        ChampOperation pComplementTech = seedChamp("PC_COMPL_TECH", "Documents complémentaires techniques",    EnumInputFieldType.FILE, false, 1, permisOp3, null);
        ChampOperation pComplementFonc = seedChamp("PC_COMPL_FONC", "Documents complémentaires fonciers",      EnumInputFieldType.FILE, false, 2, permisOp3, null);
        ChampOperation pPlanCorrige    = seedChamp("PC_PLAN_CORR",  "Plan corrigé (si réserves architecturales)", EnumInputFieldType.FILE, false, 3, permisOp3, null);
        ChampOperation pEngagement     = seedChamp("PC_ENGAGEMENT", "Lettre d'engagement du demandeur",        EnumInputFieldType.FILE, true,  4, permisOp3, null);
        ChampOperation pObsDemandeur   = seedChamp("PC_OBS_DEM",    "Observations du demandeur",               EnumInputFieldType.TEXTAREA, false, 5, permisOp3, null);
        ChampOperation pConfCompletude = seedChamp("PC_CONF_COMPL", "Je confirme avoir fourni tous les compléments demandés", EnumInputFieldType.CHECKBOX, true, 6, permisOp3, null);

        // ── Champs Op4 — Validation finale (agent) ────────────────────────────
        ChampOperation pDecision  = seedChamp("PC_DECISION",   "Décision finale",                 EnumInputFieldType.SELECT,   true,  1, permisOp4, null);
        ChampOperation pNumPermis = seedChamp("PC_NUM_PERMIS", "Numéro du permis attribué",       EnumInputFieldType.TEXT,     false, 2, permisOp4, null);
        ChampOperation pDureeVal  = seedChamp("PC_DUREE_VAL",  "Durée de validité (mois)",        EnumInputFieldType.NUMBER,   false, 3, permisOp4, null);
        ChampOperation pConditions= seedChamp("PC_CONDITIONS", "Conditions particulières",        EnumInputFieldType.TEXTAREA, false, 4, permisOp4, null);
        ChampOperation pPermisDoc = seedChamp("PC_PERMIS_DOC", "Document du permis signé (PDF)",  EnumInputFieldType.FILE,     false, 5, permisOp4, null);

        seedOption(pDecision, "Accordé",                     "ACCORDE",       true,  1);
        seedOption(pDecision, "Accordé sous conditions",     "SOUS_CONDITIONS",false,2);
        seedOption(pDecision, "Refusé",                      "REFUSE",        false, 3);

        // ── Champs Op5 — Retrait (citoyen, dernier) ───────────────────────────
        ChampOperation pConfRetrait = seedChamp("PC_CONF_RETRAIT","Confirmation de retrait en personne",  EnumInputFieldType.CHECKBOX, true, 1, permisOp5, null);
        ChampOperation pDateRetrait = seedChamp("PC_DATE_RETRAIT","Date de retrait souhaitée",            EnumInputFieldType.DATE,     true, 2, permisOp5, null);
        ChampOperation pSignature   = seedChamp("PC_SIGNATURE",   "Signature électronique du bénéficiaire (IMAGE)", EnumInputFieldType.IMAGE, true, 3, permisOp5, null);
        ChampOperation pRecuPaiement= seedChamp("PC_RECU_PAIEMENT","Reçu de paiement des frais (PDF)",   EnumInputFieldType.FILE,     true, 4, permisOp5, null);

        // ══════════════════════════════════════════════════════════════════════
        //  PROCÉDURE 2 : BOURSE D'ÉTUDES SUPÉRIEURES
        // ══════════════════════════════════════════════════════════════════════
        Procedure procBourse = seedProc(
                "PROC-BOURSE-ETUDES",
                "Demande de Bourse d'Études Supérieures",
                "Procédure de candidature et d'attribution des bourses d'études supérieures pour les étudiants burkinabè. La bourse couvre les frais de scolarité et un forfait mensuel.",
                "Relevé de notes du baccalauréat; Certificat de scolarité; Attestation de ressources des parents; Lettre de motivation; Photo d'identité",
                "60 jours ouvrables",
                BigDecimal.ZERO,
                false,
                dgBourses,
                catEducation,
                "CITOYEN,RESIDENT"
        );

        // ─────────────────────────────────────────────────────────────────────
        //  OPÉRATIONS BOURSE D'ÉTUDES
        //  Op1 [C,first] → Op2 [A] → Op3 [C, intermédiaire]
        //                               ├──→ Op4a [A] évaluation académique
        //                               └──→ Op4b [A] évaluation sociale   ← multiple suivants sur Op3
        //                Op4a + Op4b  ──→ Op5 [A] délibération              ← multiple précédents sur Op5
        //  Op5 → Op6 [C, last]
        // ─────────────────────────────────────────────────────────────────────

        Operation bourseOp1 = seedOp(
                "Dépôt de la candidature",
                procBourse, 1,
                true, false, true,
                typeSoumission
        );

        Operation bourseOp2 = seedOp(
                "Vérification de l'admissibilité",
                procBourse, 2,
                false, false, false,
                typeInstruction
        );

        Operation bourseOp3 = seedOp(
                "Compléments du candidat",
                procBourse, 3,
                true,  // isCitizenOperation — ÉTAPE CITOYENNE INTERMÉDIAIRE
                false, false,
                typeComplement
        );

        Operation bourseOp4a = seedOp(
                "Évaluation académique",
                procBourse, 4,
                false, false, false,
                typeEvaluation
        );

        Operation bourseOp4b = seedOp(
                "Évaluation de la situation sociale",
                procBourse, 4,   // même orderIndex : parallèle
                false, false, false,
                typeEvaluation
        );

        Operation bourseOp5 = seedOp(
                "Délibération de la commission",
                procBourse, 5,
                false, false, false,
                typeDeliberation
        );

        Operation bourseOp6 = seedOp(
                "Acceptation et engagement du lauréat",
                procBourse, 6,
                true,  // isCitizenOperation — ÉTAPE CITOYENNE FINALE
                true,  // isLastOperation
                false,
                typeCollecte
        );

        // Chaîne linéaire Op1 → Op2 → Op3
        linkNextPrev(bourseOp1, bourseOp2);
        linkNextPrev(bourseOp2, bourseOp3);

        // Op3 → Op4a ET Op4b  (branchement multiple depuis l'étape citoyenne)
        linkNextPrev(bourseOp3, bourseOp4a);
        linkNextPrev(bourseOp3, bourseOp4b);

        // Op4a → Op5  ET  Op4b → Op5  (convergence multiple)
        linkNextPrev(bourseOp4a, bourseOp5);
        linkNextPrev(bourseOp4b, bourseOp5);

        // Op5 → Op6
        linkNextPrev(bourseOp5, bourseOp6);

        // ── Champs Op1 — Dépôt candidature (citoyen, premier) ────────────────
        ChampOperation bNom       = seedChamp("BS_NOM",        "Nom de famille",                  EnumInputFieldType.TEXT,     true,  1,  bourseOp1, entDemandeur);
        ChampOperation bPrenom    = seedChamp("BS_PRENOM",     "Prénom(s)",                       EnumInputFieldType.TEXT,     true,  2,  bourseOp1, entDemandeur);
        ChampOperation bDateNaiss = seedChamp("BS_DATE_NAISS", "Date de naissance",               EnumInputFieldType.DATE,     true,  3,  bourseOp1, entDemandeur);
        ChampOperation bLieuNaiss = seedChamp("BS_LIEU_NAISS", "Lieu de naissance",               EnumInputFieldType.TEXT,     true,  4,  bourseOp1, entDemandeur);
        ChampOperation bTel       = seedChamp("BS_TEL",        "Téléphone",                       EnumInputFieldType.PHONE,    true,  5,  bourseOp1, entDemandeur);
        ChampOperation bEmail     = seedChamp("BS_EMAIL",      "Email",                           EnumInputFieldType.EMAIL,    true,  6,  bourseOp1, entDemandeur);
        ChampOperation bAdresse   = seedChamp("BS_ADRESSE",    "Adresse actuelle",                EnumInputFieldType.TEXTAREA, true,  7,  bourseOp1, entDemandeur);
        ChampOperation bSexe      = seedChamp("BS_SEXE",       "Sexe",                            EnumInputFieldType.SELECT,   true,  8,  bourseOp1, entDemandeur);

        ChampOperation bEtabActuel= seedChamp("BS_ETAB",       "Établissement actuel / visé",     EnumInputFieldType.TEXT,     true,  9,  bourseOp1, null);
        ChampOperation bFiliere   = seedChamp("BS_FILIERE",    "Filière d'études",                EnumInputFieldType.TEXT,     true,  10, bourseOp1, null);
        ChampOperation bNiveau    = seedChamp("BS_NIVEAU",     "Niveau d'études visé",            EnumInputFieldType.SELECT,   true,  11, bourseOp1, null);
        ChampOperation bPays      = seedChamp("BS_PAYS_ETUDE", "Pays d'études",                   EnumInputFieldType.SELECT,   true,  12, bourseOp1, null);
        ChampOperation bTypeBourse= seedChamp("BS_TYPE",       "Type de bourse demandée",         EnumInputFieldType.SELECT,   true,  13, bourseOp1, null);
        ChampOperation bMotivation= seedChamp("BS_MOTIVATION", "Lettre de motivation",            EnumInputFieldType.TEXTAREA, true,  14, bourseOp1, null);

        ChampOperation bRelNotes  = seedChamp("BS_REL_NOTES",  "Relevé de notes Bac (PDF/IMAGE)", EnumInputFieldType.FILE,     true,  15, bourseOp1, entDoc);
        ChampOperation bCertScol  = seedChamp("BS_CERT_SCOL",  "Certificat de scolarité",         EnumInputFieldType.FILE,     true,  16, bourseOp1, entDoc);
        ChampOperation bPhoto     = seedChamp("BS_PHOTO",      "Photo d'identité",                EnumInputFieldType.IMAGE,    true,  17, bourseOp1, entDoc);
        ChampOperation bAttRess   = seedChamp("BS_ATT_RESS",   "Attestation de ressources parentales", EnumInputFieldType.FILE, true, 18, bourseOp1, entDoc);

        seedOption(bSexe,       "Masculin",              "M",               true,  1);
        seedOption(bSexe,       "Féminin",               "F",               false, 2);
        seedOption(bNiveau,     "Licence (L1-L3)",       "LICENCE",         true,  1);
        seedOption(bNiveau,     "Master (M1-M2)",        "MASTER",          false, 2);
        seedOption(bNiveau,     "Doctorat",              "DOCTORAT",        false, 3);
        seedOption(bPays,       "Burkina Faso",          "BF",              true,  1);
        seedOption(bPays,       "France",                "FR",              false, 2);
        seedOption(bPays,       "Maroc",                 "MA",              false, 3);
        seedOption(bPays,       "Sénégal",               "SN",              false, 4);
        seedOption(bPays,       "Autre",                 "AUTRE",           false, 5);
        seedOption(bTypeBourse, "Bourse nationale",      "NATIONALE",       true,  1);
        seedOption(bTypeBourse, "Bourse internationale", "INTERNATIONALE",  false, 2);
        seedOption(bTypeBourse, "Bourse d'excellence",   "EXCELLENCE",      false, 3);

        // ── Champs Op2 — Vérification admissibilité (agent) ──────────────────
        ChampOperation bAdmissible = seedChamp("BS_ADMISSIBLE",  "Candidat éligible",             EnumInputFieldType.SELECT,   true,  1, bourseOp2, null);
        ChampOperation bMoyBac     = seedChamp("BS_MOY_BAC",     "Moyenne au baccalauréat",       EnumInputFieldType.NUMBER,   true,  2, bourseOp2, null);
        ChampOperation bMentionBac = seedChamp("BS_MENTION_BAC", "Mention obtenue",               EnumInputFieldType.SELECT,   true,  3, bourseOp2, null);
        ChampOperation bDocsOk     = seedChamp("BS_DOCS_OK",     "Documents de base complets",    EnumInputFieldType.SELECT,   true,  4, bourseOp2, null);
        ChampOperation bObsAdm     = seedChamp("BS_OBS_ADM",     "Observations admissibilité",    EnumInputFieldType.TEXTAREA, false, 5, bourseOp2, null);
        ChampOperation bComplesDemandes = seedChamp("BS_COMPL_DEMANDED","Documents complémentaires à fournir", EnumInputFieldType.TEXTAREA, false, 6, bourseOp2, null);

        seedOption(bAdmissible, "Oui, éligible",         "ELIGIBLE",        true,  1);
        seedOption(bAdmissible, "Non éligible",          "NON_ELIGIBLE",    false, 2);
        seedOption(bMentionBac, "Très bien",             "TB",              true,  1);
        seedOption(bMentionBac, "Bien",                  "B",               false, 2);
        seedOption(bMentionBac, "Assez bien",            "AB",              false, 3);
        seedOption(bMentionBac, "Passable",              "P",               false, 4);
        seedOption(bDocsOk,     "Complets",              "COMPLETS",        true,  1);
        seedOption(bDocsOk,     "Incomplets",            "INCOMPLETS",      false, 2);

        // ── Champs Op3 — Compléments candidat (citoyen, intermédiaire) ───────
        ChampOperation bCompl1   = seedChamp("BS_COMPL_1",    "Document complémentaire demandé 1",  EnumInputFieldType.FILE,     false, 1, bourseOp3, null);
        ChampOperation bCompl2   = seedChamp("BS_COMPL_2",    "Document complémentaire demandé 2",  EnumInputFieldType.FILE,     false, 2, bourseOp3, null);
        ChampOperation bDiplomes = seedChamp("BS_DIPLOMES",   "Diplômes et attestations supplémentaires", EnumInputFieldType.FILE, false, 3, bourseOp3, null);
        ChampOperation bRevParents = seedChamp("BS_REV_PAR",  "Revenus annuels des parents (FCFA)", EnumInputFieldType.NUMBER,   true,  4, bourseOp3, null);
        ChampOperation bFratrie  = seedChamp("BS_FRATRIE",    "Nombre de frères et sœurs à charge", EnumInputFieldType.NUMBER,   false, 5, bourseOp3, null);
        ChampOperation bHandicap = seedChamp("BS_HANDICAP",   "Situation de handicap éventuelle",   EnumInputFieldType.SELECT,   false, 6, bourseOp3, null);
        ChampOperation bProjPro  = seedChamp("BS_PROJ_PRO",   "Projet professionnel après études",  EnumInputFieldType.TEXTAREA, true,  7, bourseOp3, null);
        ChampOperation bConfCompl= seedChamp("BS_CONF_COMPL", "Je certifie que mes informations sont exactes", EnumInputFieldType.CHECKBOX, true, 8, bourseOp3, null);

        seedOption(bHandicap, "Aucun handicap",          "AUCUN",           true,  1);
        seedOption(bHandicap, "Handicap moteur",         "MOTEUR",          false, 2);
        seedOption(bHandicap, "Handicap visuel",         "VISUEL",          false, 3);
        seedOption(bHandicap, "Autre",                   "AUTRE",           false, 4);

        // ── Champs Op4a — Évaluation académique (agent) ───────────────────────
        ChampOperation bScoreAca  = seedChamp("BS_SCORE_ACA",  "Score académique (/100)",         EnumInputFieldType.NUMBER,   true,  1, bourseOp4a, null);
        ChampOperation bNoteAca   = seedChamp("BS_NOTE_ACA",   "Niveau académique estimé",        EnumInputFieldType.SELECT,   true,  2, bourseOp4a, null);
        ChampOperation bCoherence = seedChamp("BS_COHERENCE",  "Cohérence du projet d'études",    EnumInputFieldType.SELECT,   true,  3, bourseOp4a, null);
        ChampOperation bRapAca    = seedChamp("BS_RAP_ACA",    "Rapport d'évaluation académique", EnumInputFieldType.TEXTAREA, false, 4, bourseOp4a, null);

        seedOption(bNoteAca,   "Excellent (>16/20)",     "EXCELLENT",       true,  1);
        seedOption(bNoteAca,   "Bon (14-16/20)",         "BON",             false, 2);
        seedOption(bNoteAca,   "Satisfaisant (12-14/20)","SATISFAISANT",    false, 3);
        seedOption(bNoteAca,   "Insuffisant (<12/20)",   "INSUFFISANT",     false, 4);
        seedOption(bCoherence, "Très cohérent",          "TRES_COHERENT",   true,  1);
        seedOption(bCoherence, "Cohérent",               "COHERENT",        false, 2);
        seedOption(bCoherence, "Peu cohérent",           "PEU_COHERENT",    false, 3);

        // ── Champs Op4b — Évaluation sociale (agent) ──────────────────────────
        ChampOperation bScoreSoc  = seedChamp("BS_SCORE_SOC",  "Score social (/100)",             EnumInputFieldType.NUMBER,   true,  1, bourseOp4b, null);
        ChampOperation bSitFinanc = seedChamp("BS_SIT_FINANC", "Situation financière de la famille",EnumInputFieldType.SELECT,  true,  2, bourseOp4b, null);
        ChampOperation bVulnerabil= seedChamp("BS_VULNERAB",   "Facteurs de vulnérabilité",       EnumInputFieldType.SELECT,   false, 3, bourseOp4b, null);
        ChampOperation bRapSoc    = seedChamp("BS_RAP_SOC",    "Rapport d'évaluation sociale",    EnumInputFieldType.TEXTAREA, false, 4, bourseOp4b, null);

        seedOption(bSitFinanc, "Très modeste (<150k/an)","TRES_MODESTE",    true,  1);
        seedOption(bSitFinanc, "Modeste (150k-300k/an)", "MODESTE",         false, 2);
        seedOption(bSitFinanc, "Moyen (300k-600k/an)",   "MOYEN",           false, 3);
        seedOption(bSitFinanc, "Aisée (>600k/an)",       "AISEE",           false, 4);
        seedOption(bVulnerabil,"Orphelin(e)",             "ORPHELIN",        false, 1);
        seedOption(bVulnerabil,"Famille monoparentale",   "MONOPARENTAL",    false, 2);
        seedOption(bVulnerabil,"Aucun facteur",           "AUCUN",           true,  3);

        // ── Champs Op5 — Délibération commission (agent) ──────────────────────
        ChampOperation bDecision  = seedChamp("BS_DECISION",   "Décision de la commission",       EnumInputFieldType.SELECT,   true,  1, bourseOp5, null);
        ChampOperation bMontant   = seedChamp("BS_MONTANT",    "Montant mensuel accordé (FCFA)",  EnumInputFieldType.NUMBER,   false, 2, bourseOp5, null);
        ChampOperation bDuree     = seedChamp("BS_DUREE",      "Durée d'attribution (mois)",      EnumInputFieldType.NUMBER,   false, 3, bourseOp5, null);
        ChampOperation bNumAttrib = seedChamp("BS_NUM_ATTRIB", "Numéro d'attribution",            EnumInputFieldType.TEXT,     false, 4, bourseOp5, null);
        ChampOperation bCondBourse= seedChamp("BS_COND",       "Conditions et obligations du boursier", EnumInputFieldType.TEXTAREA, false, 5, bourseOp5, null);
        ChampOperation bProcesVerb= seedChamp("BS_PV",         "Procès-verbal de délibération (PDF)", EnumInputFieldType.FILE,  false, 6, bourseOp5, null);

        seedOption(bDecision, "Bourse accordée",          "ACCORDEE",        true,  1);
        seedOption(bDecision, "Bourse accordée partielle","PARTIELLE",        false, 2);
        seedOption(bDecision, "Liste d'attente",          "ATTENTE",         false, 3);
        seedOption(bDecision, "Dossier rejeté",           "REJETE",          false, 4);

        // ── Champs Op6 — Acceptation lauréat (citoyen, dernier) ──────────────
        ChampOperation bAcceptation = seedChamp("BS_ACCEPTATION","J'accepte les conditions de la bourse", EnumInputFieldType.CHECKBOX, true, 1, bourseOp6, null);
        ChampOperation bRIB         = seedChamp("BS_RIB",        "RIB / Coordonnées bancaires (PDF)",     EnumInputFieldType.FILE,     true, 2, bourseOp6, null);
        ChampOperation bSignatEngag = seedChamp("BS_SIGNAT",     "Signature de l'engagement (IMAGE)",     EnumInputFieldType.IMAGE,    true, 3, bourseOp6, null);
        ChampOperation bDateDebut   = seedChamp("BS_DATE_DEBUT", "Date de prise d'effet souhaitée",       EnumInputFieldType.DATE,     true, 4, bourseOp6, null);

        // ══════════════════════════════════════════════════════════════════════
        //  LIENS ORGANISATIONS
        // ══════════════════════════════════════════════════════════════════════
        linkUserToOrg(agentTechnique,  dgUrban);
        linkUserToOrg(agentFoncier,    dgUrban);
        linkUserToOrg(agentValidateur, dgUrban);
        linkUserToOrg(adminUrban,      dgUrban);
        linkUserToOrg(adminUrban,      minUrban);

        linkUserToOrg(agentAdmissib,   dgBourses);
        linkUserToOrg(agentAcademique, dgBourses);
        linkUserToOrg(agentSocial,     dgBourses);
        linkUserToOrg(agentCommission, dgBourses);
        linkUserToOrg(adminEdu,        dgBourses);
        linkUserToOrg(adminEdu,        minEdu);

        // ══════════════════════════════════════════════════════════════════════
        //  LIENS PROCÉDURES (admin)
        // ══════════════════════════════════════════════════════════════════════
        linkUserToProc(adminUrban, procPermis);
        linkUserToProc(adminEdu,   procBourse);

        // ══════════════════════════════════════════════════════════════════════
        //  LIENS OPÉRATIONS → AGENTS
        // ══════════════════════════════════════════════════════════════════════
        // Permis de construire
        linkUserToOp(agentTechnique,  permisOp2a);
        linkUserToOp(agentFoncier,    permisOp2b);
        linkUserToOp(agentValidateur, permisOp4);

        // Bourse d'études
        linkUserToOp(agentAdmissib,   bourseOp2);
        linkUserToOp(agentAcademique, bourseOp4a);
        linkUserToOp(agentSocial,     bourseOp4b);
        linkUserToOp(agentCommission, bourseOp5);

        log.info("✅ ProcedureComplexeSeeder terminé avec succès.");
        log.info("   • PROC-PERMIS-CONSTRUIRE : {} opérations (branchement Op1→Op2a+Op2b, convergence Op2a+Op2b→Op3)", 5);
        log.info("   • PROC-BOURSE-ETUDES     : {} opérations (étape citoyenne intermédiaire Op3, branchement Op3→Op4a+Op4b, convergence Op4a+Op4b→Op5)", 6);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Helpers (même pattern que DatabaseSeeder)
    // ══════════════════════════════════════════════════════════════════════════

    private void seedExtraTypeOperations() {
        seedTypeOp("COMPLEMENT",    "Compléments citoyens",    "Étape de collecte de compléments d'information auprès du citoyen");
        seedTypeOp("EVALUATION",    "Évaluation thématique",   "Étape d'évaluation spécialisée par un expert");
        seedTypeOp("DELIBERATION",  "Délibération collégiale", "Décision prise en commission ou en collège");
    }

    private void seedExtraEntityObjects() {
        seedEntityObject("TERRAIN",  "Terrain / Parcelle",   "Informations relatives à la parcelle ou au terrain concerné");
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

    private void seedEntityObject(String code, String name, String description) {
        entityObjectRepository.findByCodeAndDeletedFalse(code).ifPresentOrElse(
                e -> log.debug("✔ EntityObject existe: {}", code),
                () -> {
                    entityObjectRepository.save(EntityObject.builder()
                            .code(code).name(name).description(description).isActive(true).build());
                    log.info("✅ EntityObject créé: {}", code);
                });
    }

    private Organisation seedOrg(String code, String name, String description,
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
                          String firstName, String lastName, String idUnique) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .firstName(firstName)
                    .lastName(lastName)
                    .userStatus(UserStatus.CITOYEN)
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

    private Procedure seedProc(String code, String name, String description,
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

    private Operation seedOp(String name, Procedure procedure, int orderIndex,
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
            String type = isCitizen ? "[CITOYEN]" : "[AGENT]";
            String pos  = isFirst ? " [PREMIÈRE]" : isLast ? " [DERNIÈRE]" : "";
            log.info("✅ Opération créée: {} '{}' (ordre {})", type + pos, name, orderIndex);
            return op;
        });
    }

    private void linkNextPrev(Operation from, Operation to) {
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
            log.info("   ✅ Lien: '{}' → '{}'", f.getName(), t.getName());
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
            log.info("   ✅ Champ: '{}' [{}] ({})", label, fieldType, required ? "obligatoire" : "facultatif");
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
                    log.info("      ✅ Option: '{}'", label);
                });
    }

    private void linkUserToOrg(User user, Organisation org) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if (u == null) return;
        if (!u.getOrganisations().contains(org)) {
            u.getOrganisations().add(org);
            userRepository.save(u);
        }
    }

    private void linkUserToProc(User user, Procedure proc) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if (u == null) return;
        if (!u.getProcedures().contains(proc)) {
            u.getProcedures().add(proc);
            userRepository.save(u);
        }
    }

    private void linkUserToOp(User user, Operation op) {
        User u = userRepository.findById(user.getId()).orElse(null);
        if (u == null) return;
        if (!u.getOperations().contains(op)) {
            u.getOperations().add(op);
            userRepository.save(u);
            log.info("✅ Agent '{}' → opération '{}'", u.getEmail(), op.getName());
        }
    }
}
