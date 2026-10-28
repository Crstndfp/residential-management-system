package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.AssetStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAssetStatusHistoryRepository extends JpaRepository<AssetStatusHistory, UUID> {
    @Query("SELECT h FROM AssetStatusHistory h WHERE h.asset.id = :assetId ORDER BY h.createdAt DESC")
    List<AssetStatusHistory> findByAssetIdOrderByCreatedAtDesc(@Param("assetId") UUID assetId);
}
