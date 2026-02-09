package com.deliacte.repository;

import com.deliacte.entity.OptionChampOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OptionChampOperationRepository
        extends JpaRepository<OptionChampOperation, UUID> {

    List<OptionChampOperation> findByChampOperationIdOrderByOrderIndexAsc(UUID champOperationId);

    List<OptionChampOperation> findByChampOperationId(UUID champOperationId);
    List<OptionChampOperation> findByIdIn(List<UUID> ids);

}
