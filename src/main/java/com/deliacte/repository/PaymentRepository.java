package com.deliacte.repository;

import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierOperation;
import com.deliacte.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Vérifier si un paiement existe déjà pour un dossier
    boolean existsByDossier(Dossier dossier);

    // Vérifier si un paiement existe déjà pour une opération
    boolean existsByDossierOperation(DossierOperation dossierOperation);

    // Vérifier si un paiement existe déjà pour une opération liée à un dossier
    boolean existsByDossierOperation_Dossier(Dossier dossier);

    // Retrouver un paiement par son mappedOrderId (référence Arzeka)
    Optional<Payment> findByMappedOrderId(String mappedOrderId);

    // Retrouver un paiement par dossier
    Optional<Payment> findByDossier(Dossier dossier);

    // Retrouver un paiement par opération
    Optional<Payment> findByDossierOperation(DossierOperation dossierOperation);
}
