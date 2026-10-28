package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.FixedAsset;
import com.residencial.finance.domain.repository.FixedAssetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FixedAssetRepositoryAdapter implements FixedAssetRepository {

    private final JpaFixedAssetRepository jpaFixedAssetRepository;

    public FixedAssetRepositoryAdapter(JpaFixedAssetRepository jpaFixedAssetRepository) {
        this.jpaFixedAssetRepository = jpaFixedAssetRepository;
    }

    @Override
    public FixedAsset save(FixedAsset asset) {
        return jpaFixedAssetRepository.save(asset);
    }

    @Override
    public Optional<FixedAsset> findById(UUID id) {
        return jpaFixedAssetRepository.findById(id);
    }

    @Override
    public Optional<FixedAsset> findByCode(String code) {
        return jpaFixedAssetRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaFixedAssetRepository.existsByCode(code);
    }

    @Override
    public List<FixedAsset> findAll(AssetCategory category, AssetStatus status) {
        return jpaFixedAssetRepository.findAllWithFilters(category, status);
    }
}
