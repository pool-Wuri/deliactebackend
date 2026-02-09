package com.deliacte.repository;

import com.deliacte.entity.TypeOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeOperationRepository extends JpaRepository<TypeOperation, UUID> {
    
    Optional<TypeOperation> findByIdAndDeletedFalse(UUID id);
    
    Optional<TypeOperation> findByCodeAndDeletedFalse(String code);
    
    Page<TypeOperation> findAllByDeletedFalse(Pageable pageable);
    
    List<TypeOperation> findByActiveAndDeletedFalse(Boolean active);
    
    Boolean existsByCodeAndDeletedFalse(String code);
    
    long countByDeletedFalse();
}
