package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.FixedAssetDto;
import com.residencial.finance.application.dto.UpdateAssetStatusRequest;
import com.residencial.finance.domain.exception.AssetNotFoundException;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.AssetStatusHistory;
import com.residencial.finance.domain.model.FixedAsset;
import com.residencial.finance.domain.repository.AssetStatusHistoryRepository;
import com.residencial.finance.domain.repository.FixedAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateAssetStatusUseCase {

    private final FixedAssetRepository fixedAssetRepository;
    private final AssetStatusHistoryRepository assetStatusHistoryRepository;

    public UpdateAssetStatusUseCase(FixedAssetRepository fixedAssetRepository,
                                    AssetStatusHistoryRepository assetStatusHistoryRepository) {
        this.fixedAssetRepository = fixedAssetRepository;
        this.assetStatusHistoryRepository = assetStatusHistoryRepository;
    }

    @Transactional
    public FixedAssetDto execute(UUID assetId, UpdateAssetStatusRequest request) {
        if (assetId == null) {
            throw new IllegalArgumentException("Asset ID cannot be null.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update asset status request cannot be null.");
        }

        FixedAsset asset = fixedAssetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException("Fixed asset with ID '" + assetId + "' was not found."));

        AssetStatus previousStatus = asset.getStatus();
        asset.updateStatus(request.newStatus(), request.reason());

        AssetStatusHistory history = new AssetStatusHistory(
                asset,
                previousStatus,
                request.newStatus(),
                request.reason(),
                request.changedByUserId()
        );

        assetStatusHistoryRepository.save(history);
        FixedAsset updated = fixedAssetRepository.save(asset);

        return new FixedAssetDto(
                updated.getId(),
                updated.getCode(),
                updated.getName(),
                updated.getCategory(),
                updated.getPhysicalLocation(),
                updated.getAcquisitionCost(),
                updated.getAcquisitionDate(),
                updated.getStatus(),
                updated.getNotes(),
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );
    }
}
