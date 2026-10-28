package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.ApplyFineRequest;
import com.residencial.finance.application.dto.ApplyFineResponse;
import com.residencial.finance.domain.exception.FineTypeNotFoundException;
import com.residencial.finance.domain.model.Fine;
import com.residencial.finance.domain.model.FineType;
import com.residencial.finance.domain.repository.FineRepository;
import com.residencial.finance.domain.repository.FineTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplyFineToHouseUseCase {

    private final FineRepository fineRepository;
    private final FineTypeRepository fineTypeRepository;

    public ApplyFineToHouseUseCase(FineRepository fineRepository, FineTypeRepository fineTypeRepository) {
        this.fineRepository = fineRepository;
        this.fineTypeRepository = fineTypeRepository;
    }

    public ApplyFineResponse execute(ApplyFineRequest request) {
        String fineCode = request.fineTypeCode().trim().toUpperCase();
        FineType fineType = fineTypeRepository.findByCode(fineCode)
                .filter(FineType::isActive)
                .orElseThrow(() -> new FineTypeNotFoundException(fineCode));

        Fine fine = new Fine(
                request.houseId(),
                request.houseNumber(),
                fineType,
                request.customAmount(),
                request.reason(),
                request.evidenceUrl(),
                request.appliedByUserId()
        );

        Fine savedFine = fineRepository.save(fine);

        return new ApplyFineResponse(
                savedFine.getId(),
                savedFine.getHouseId(),
                savedFine.getHouseNumber(),
                fineType.getCode(),
                fineType.getName(),
                savedFine.getAmount(),
                savedFine.getReason(),
                savedFine.getEvidenceUrl(),
                savedFine.getStatus(),
                savedFine.getCreatedAt()
        );
    }
}
