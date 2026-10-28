package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.RegisterAssetRequest;
import com.residencial.finance.application.dto.RegisterAssetResponse;
import com.residencial.finance.domain.exception.DuplicateAssetCodeException;
import com.residencial.finance.domain.model.FixedAsset;
import com.residencial.finance.domain.repository.FixedAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterFixedAssetUseCase {

    private final FixedAssetRepository fixedAssetRepository;

    public RegisterFixedAssetUseCase(FixedAssetRepository fixedAssetRepository) {
        this.fixedAssetRepository = fixedAssetRepository;
    }

    @Transactional
    public RegisterAssetResponse execute(RegisterAssetRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Asset registration request cannot be null.");
        }

        String normalizedCode = request.code().trim().toUpperCase();
        if (fixedAssetRepository.existsByCode(normalizedCode)) {
            throw new DuplicateAssetCodeException(normalizedCode);
        }

        FixedAsset asset = new FixedAsset(
                normalizedCode,
                request.name(),
                request.category(),
                request.physicalLocation(),
                request.acquisitionCost(),
                request.acquisitionDate(),
                request.status(),
                request.notes()
        );

        FixedAsset saved = fixedAssetRepository.save(asset);

        return new RegisterAssetResponse(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getCategory(),
                saved.getPhysicalLocation(),
                saved.getAcquisitionCost(),
                saved.getAcquisitionDate(),
                saved.getStatus(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }
}
