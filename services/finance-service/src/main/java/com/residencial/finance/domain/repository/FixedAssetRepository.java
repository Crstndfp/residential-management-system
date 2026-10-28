package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.FixedAsset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixedAssetRepository {
    FixedAsset save(FixedAsset asset);
    Optional<FixedAsset> findById(UUID id);
    Optional<FixedAsset> findByCode(String code);
    boolean existsByCode(String code);
    List<FixedAsset> findAll(AssetCategory category, AssetStatus status);
}
