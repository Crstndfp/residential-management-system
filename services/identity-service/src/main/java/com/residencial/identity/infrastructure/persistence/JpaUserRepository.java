package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.ResidentialRole;
import com.residencial.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByHouseIdAndResidentialRole(UUID houseId, ResidentialRole residentialRole);
}
