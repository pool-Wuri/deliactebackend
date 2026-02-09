package com.deliacte.repository;

import com.deliacte.entity.Parameter;
import com.deliacte.enums.ParameterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParameterRepository extends JpaRepository<Parameter, UUID> {

    Optional<Parameter> findByTypeAndCode(ParameterType type, String code);

    List<Parameter> findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(ParameterType type);
}
