package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.Casa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCasaRepository extends JpaRepository<Casa, UUID> {
    Optional<Casa> findByNumeroIdentificador(String numeroIdentificador);
    boolean existsByNumeroIdentificador(String numeroIdentificador);
}
