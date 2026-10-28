package com.residencial.parking.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface FinanceChargePort {
    UUID chargeParkingFine(UUID houseId, BigDecimal amount, String concept, String referenceId);
}
