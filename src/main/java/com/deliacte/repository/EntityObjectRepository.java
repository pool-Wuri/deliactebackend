package com.deliacte.repository;

import com.deliacte.entity.EntityObject;
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
public interface EntityObjectRepository extends JpaRepository<EntityObject, UUID> {

    Optional<EntityObject> findByIdAndDeletedFalse(UUID id);

    Optional<EntityObject> findByCodeAndDeletedFalse(String code);

    Page<EntityObject> findByDeletedFalse(Pageable pageable);

    Page<EntityObject> findByIsActiveAndDeletedFalse(Boolean isActive, Pageable pageable);


    @Query("""
        SELECT eo
        FROM Operation o
        JOIN o.entityObjects eo
        WHERE o.id = :operationId
    """)
    List<EntityObject> findEntityObjectsByOperationId(@Param("operationId") UUID operationId);

    Boolean existsByCodeAndDeletedFalse(String code);
}
