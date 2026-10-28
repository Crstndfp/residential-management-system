package com.residencial.parking.infrastructure.client;

import com.residencial.parking.domain.port.FinanceChargePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class FinanceClientAdapter implements FinanceChargePort {

    private static final Logger log = LoggerFactory.getLogger(FinanceClientAdapter.class);

    @Override
    public UUID chargeParkingFine(UUID houseId, BigDecimal amount, String concept, String referenceId) {
        UUID chargeId = UUID.randomUUID();
        log.info("[FINANCE-CLIENT] Dispatched fine charge [${}] to house [{}] - Concept: [{}] (ChargeId: {})",
                amount, houseId, concept, chargeId);
        return chargeId;
    }
}
