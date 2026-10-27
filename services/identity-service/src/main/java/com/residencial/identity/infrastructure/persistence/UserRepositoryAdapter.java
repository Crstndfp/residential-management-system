package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.ResidentialRole;
import com.residencial.identity.domain.model.User;
import com.residencial.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public boolean existsByHouseIdAndResidentialRole(UUID houseId, ResidentialRole residentialRole) {
        return jpaUserRepository.existsByHouseIdAndResidentialRole(houseId, residentialRole);
    }
}
