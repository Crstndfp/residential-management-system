package com.residencial.identity.domain.repository;

import com.residencial.identity.domain.model.RolResidencial;
import com.residencial.identity.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(UUID id);
    Optional<Usuario> findByEmail(String email);
    boolean existsByCasaIdAndRolResidencial(UUID casaId, RolResidencial rolResidencial);
}
