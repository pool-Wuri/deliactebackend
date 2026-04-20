package com.deliacte.repository;

import com.deliacte.entity.Dossier;
import com.deliacte.entity.DossierOperation;
import com.deliacte.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByDossier(Dossier dossier);
    boolean existsByDossierOperation(DossierOperation dossierOperation);
    boolean existsByDossierOperation_Dossier(Dossier dossier);

    Optional<Payment> findByMappedOrderId(String mappedOrderId);
    Optional<Payment> findByDossier(Dossier dossier);
    Optional<Payment> findByDossierOperation(DossierOperation dossierOperation);

    List<Payment> findAllByDossier(Dossier dossier);
    List<Payment> findAllByDossierOperation_Dossier(Dossier dossier);

    boolean existsByDossierAndStatusAndPaymentInfo(Dossier dossier, String status, String paymentInfo);
    boolean existsByDossierOperationAndStatusAndPaymentInfo(DossierOperation op, String status, String paymentInfo);
}
