package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.RolResidencial;
import com.residencial.identity.domain.model.Usuario;
import com.residencial.identity.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final JpaUsuarioRepository jpaUsuarioRepository;

    public UsuarioRepositoryAdapter(JpaUsuarioRepository jpaUsuarioRepository) {
        this.jpaUsuarioRepository = jpaUsuarioRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        return jpaUsuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaUsuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaUsuarioRepository.findByEmail(email);
    }

    @Override
    public boolean existsByCasaIdAndRolResidencial(UUID casaId, RolResidencial rolResidencial) {
        return jpaUsuarioRepository.existsByCasaIdAndRolResidencial(casaId, rolResidencial);
    }
}
