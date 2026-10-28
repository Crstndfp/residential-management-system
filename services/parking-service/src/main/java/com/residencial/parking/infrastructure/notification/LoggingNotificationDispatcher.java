package com.residencial.parking.infrastructure.notification;

import com.residencial.parking.domain.port.NotificationDispatcherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LoggingNotificationDispatcher implements NotificationDispatcherPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationDispatcher.class);

    @Override
    public void notifyVisitorEntry(UUID houseId, String houseNumber, String plate, String spotNumber) {
        log.info("[PUSH-DISPATCH] Notified Owner of house [{}] ({}): Vehicle [{}] entered and was assigned parking spot [{}]",
                houseId, houseNumber, plate, spotNumber);
    }

    @Override
    public void notifyParkingFineApplied(UUID houseId, String houseNumber, String plate, BigDecimal fineAmount, String reason) {
        log.info("[PUSH-DISPATCH] Notified Owner of house [{}] ({}): Fine applied for [${}] on vehicle [{}] - Reason: {}",
                houseId, houseNumber, fineAmount, plate, reason);
    }
}
