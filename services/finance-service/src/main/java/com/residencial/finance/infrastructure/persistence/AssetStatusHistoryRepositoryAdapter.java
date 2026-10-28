package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.AssetStatusHistory;
import com.residencial.finance.domain.repository.AssetStatusHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AssetStatusHistoryRepositoryAdapter implements AssetStatusHistoryRepository {

    private final JpaAssetStatusHistoryRepository jpaAssetStatusHistoryRepository;

    public AssetStatusHistoryRepositoryAdapter(JpaAssetStatusHistoryRepository jpaAssetStatusHistoryRepository) {
        this.jpaAssetStatusHistoryRepository = jpaAssetStatusHistoryRepository;
    }

    @Override
    public AssetStatusHistory save(AssetStatusHistory history) {
        return jpaAssetStatusHistoryRepository.save(history);
    }

    @Override
    public List<AssetStatusHistory> findByAssetId(UUID assetId) {
        return jpaAssetStatusHistoryRepository.findByAssetIdOrderByCreatedAtDesc(assetId);
    }
}
