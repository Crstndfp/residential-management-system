package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaFixedAssetRepository extends JpaRepository<FixedAsset, UUID> {
    Optional<FixedAsset> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT a FROM FixedAsset a WHERE " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.createdAt DESC")
    List<FixedAsset> findAllWithFilters(@Param("category") AssetCategory category,
                                        @Param("status") AssetStatus status);
}
