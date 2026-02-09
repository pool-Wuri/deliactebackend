package com.deliacte.repository;

import com.deliacte.entity.Dossier;
import com.deliacte.enums.DossierOperationStatus;
import com.deliacte.enums.DossierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, UUID> {



    Optional<Dossier> findByDossierNumber(String numeroDossier);

    @Query("""
        SELECT d FROM Dossier d
        JOIN FETCH d.procedure p
        WHERE d.user.id = :userId
        ORDER BY d.submittedAt DESC
    """)
    List<Dossier> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT d FROM Dossier d LEFT JOIN FETCH d.procedure WHERE d.dossierNumber = :numero")
    Optional<Dossier> findByDossierNumberWithProcedure(@Param("numero") String numero);



    @Query("""
    SELECT DISTINCT d
    FROM Dossier d
    JOIN FETCH d.procedure p
    JOIN FETCH d.user u
    JOIN d.operationSteps do
    WHERE do.operation.id = :operationId
      AND do.status = :status
    ORDER BY d.submittedAt DESC
""")

    List<Dossier> findAllByOperationId(
            @Param("operationId") UUID operationId,
            @Param("status") DossierOperationStatus status
    );


}
