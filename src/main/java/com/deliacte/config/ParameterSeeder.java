package com.deliacte.config;

import com.deliacte.entity.Parameter;
import com.deliacte.enums.ParameterType;
import com.deliacte.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ParameterSeeder {

    private final ParameterRepository parameterRepository;

    @Bean
    @org.springframework.core.annotation.Order(1)
    CommandLineRunner seedParameters() {
        return args -> {

            /* ============================
             * CATÉGORIES DE PROCÉDURES
             * ============================ */
            List<Parameter> procedureCategories = List.of(

                    buildParameter(
                            ParameterType.PROCEDURE_CATEGORY,
                            "IDENTITY",
                            "Identité & État civil",
                            "Documents d'identité et état civil",
                            1
                    ),
                    buildParameter(
                            ParameterType.PROCEDURE_CATEGORY,
                            "TRAVEL",
                            "Voyage & Immigration",
                            "Voyage, visa et immigration",
                            2
                    ),
                    buildParameter(
                            ParameterType.PROCEDURE_CATEGORY,
                            "PROPERTY",
                            "Propriété & Foncier",
                            "Questions foncières et immobilières",
                            3
                    ),
                    buildParameter(
                            ParameterType.PROCEDURE_CATEGORY,
                            "BUSINESS",
                            "Commerce & Entreprise",
                            "Création et gestion d'entreprise",
                            4
                    ),
                    buildParameter(
                            ParameterType.PROCEDURE_CATEGORY,
                            "EDUCATION",
                            "Éducation",
                            "Éducation et formation",
                            5
                    ),
                    buildParameter(
                            ParameterType.PROCEDURE_CATEGORY,
                            "HEALTH",
                            "Santé",
                            "Services de santé",
                            6
                    )
            );

            /* ============================
             * TYPES D’ORGANISATIONS
             * ============================ */
            List<Parameter> organisationTypes = List.of(

                    buildParameter(
                            ParameterType.ORGANISATION_TYPE,
                            "MAIRIE",
                            "Mairie",
                            "Administration municipale",
                            1
                    ),
                    buildParameter(
                            ParameterType.ORGANISATION_TYPE,
                            "PREFECTURE",
                            "Préfecture",
                            "Administration préfectorale",
                            2
                    ),
                    buildParameter(
                            ParameterType.ORGANISATION_TYPE,
                            "MIN_JUSTICE",
                            "Ministère de la Justice",
                            "Institution judiciaire",
                            3
                    ),
                    buildParameter(
                            ParameterType.ORGANISATION_TYPE,
                            "MIN_SECURITY",
                            "Ministère de la Sécurité",
                            "Sécurité intérieure",
                            4
                    ),
                    buildParameter(
                            ParameterType.ORGANISATION_TYPE,
                            "MIN_FOREIGN_AFFAIRS",
                            "Ministère des Affaires Étrangères",
                            "Relations extérieures",
                            5
                    )
            );

            seedList(procedureCategories);
            seedList(organisationTypes);
        };
    }

    /* ============================
     * MÉTHODES UTILITAIRES
     * ============================ */

    private void seedList(List<Parameter> parameters) {
        parameters.forEach(parameter ->
                parameterRepository.findByTypeAndCode(parameter.getType(), parameter.getCode())
                        .ifPresentOrElse(
                                p -> System.out.println("✔ Parameter already exists: "
                                        + p.getType() + " / " + p.getCode()),
                                () -> {
                                    parameterRepository.save(parameter);
                                    System.out.println("✅ Parameter created: "
                                            + parameter.getType() + " / " + parameter.getCode());
                                }
                        )
        );
    }

    private Parameter buildParameter(
            ParameterType type,
            String code,
            String label,
            String description,
            Integer displayOrder
    ) {
        return Parameter.builder()
                .type(type)
                .code(code)
                .label(label)
                .description(description)
                .displayOrder(displayOrder)
                .isActive(true)
                .build();
    }
}
