package com.residencial.identity.domain.repository;

import com.residencial.identity.domain.model.Casa;
import java.util.Optional;
import java.util.UUID;

public interface CasaRepository {
    Casa save(Casa casa);
    Optional<Casa> findById(UUID id);
    Optional<Casa> findByNumeroIdentificador(String numeroIdentificador);
    boolean existsByNumeroIdentificador(String numeroIdentificador);
}
