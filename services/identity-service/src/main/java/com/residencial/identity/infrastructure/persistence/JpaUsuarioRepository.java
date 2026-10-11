package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.RolResidencial;
import com.residencial.identity.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByCasaIdAndRolResidencial(UUID casaId, RolResidencial rolResidencial);
}
