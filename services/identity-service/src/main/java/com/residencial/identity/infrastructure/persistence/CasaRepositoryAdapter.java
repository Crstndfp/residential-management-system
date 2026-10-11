package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.Casa;
import com.residencial.identity.domain.repository.CasaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CasaRepositoryAdapter implements CasaRepository {

    private final JpaCasaRepository jpaCasaRepository;

    public CasaRepositoryAdapter(JpaCasaRepository jpaCasaRepository) {
        this.jpaCasaRepository = jpaCasaRepository;
    }

    @Override
    public Casa save(Casa casa) {
        return jpaCasaRepository.save(casa);
    }

    @Override
    public Optional<Casa> findById(UUID id) {
        return jpaCasaRepository.findById(id);
    }

    @Override
    public Optional<Casa> findByNumeroIdentificador(String numeroIdentificador) {
        return jpaCasaRepository.findByNumeroIdentificador(numeroIdentificador);
    }

    @Override
    public boolean existsByNumeroIdentificador(String numeroIdentificador) {
        return jpaCasaRepository.existsByNumeroIdentificador(numeroIdentificador);
    }
}
