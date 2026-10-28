package com.residencial.parking.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface NotificationDispatcherPort {
    void notifyVisitorEntry(UUID houseId, String houseNumber, String plate, String spotNumber);
    void notifyParkingFineApplied(UUID houseId, String houseNumber, String plate, BigDecimal fineAmount, String reason);
}
