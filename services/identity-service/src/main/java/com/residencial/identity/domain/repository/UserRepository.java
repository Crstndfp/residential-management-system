package com.residencial.identity.domain.repository;

import com.residencial.identity.domain.model.ResidentialRole;
import com.residencial.identity.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByHouseIdAndResidentialRole(UUID houseId, ResidentialRole residentialRole);
}
