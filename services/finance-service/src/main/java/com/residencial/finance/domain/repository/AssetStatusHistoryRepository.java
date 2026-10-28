package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.AssetStatusHistory;

import java.util.List;
import java.util.UUID;

public interface AssetStatusHistoryRepository {
    AssetStatusHistory save(AssetStatusHistory history);
    List<AssetStatusHistory> findByAssetId(UUID assetId);
}
