package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.FixedAssetDto;
import com.residencial.finance.domain.model.AssetCategory;
import com.residencial.finance.domain.model.AssetStatus;
import com.residencial.finance.domain.model.FixedAsset;
import com.residencial.finance.domain.repository.FixedAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListFixedAssetsUseCase {

    private final FixedAssetRepository fixedAssetRepository;

    public ListFixedAssetsUseCase(FixedAssetRepository fixedAssetRepository) {
        this.fixedAssetRepository = fixedAssetRepository;
    }

    @Transactional(readOnly = true)
    public List<FixedAssetDto> execute(AssetCategory category, AssetStatus status) {
        List<FixedAsset> assets = fixedAssetRepository.findAll(category, status);
        return assets.stream()
                .map(a -> new FixedAssetDto(
                        a.getId(),
                        a.getCode(),
                        a.getName(),
                        a.getCategory(),
                        a.getPhysicalLocation(),
                        a.getAcquisitionCost(),
                        a.getAcquisitionDate(),
                        a.getStatus(),
                        a.getNotes(),
                        a.getCreatedAt(),
                        a.getUpdatedAt()
                ))
                .toList();
    }
}
