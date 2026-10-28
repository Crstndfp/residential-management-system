package com.residencial.amenities.infrastructure.notification;

import com.residencial.amenities.domain.model.TicketStatus;
import com.residencial.amenities.domain.port.NotificationDispatcherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingNotificationDispatcher implements NotificationDispatcherPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationDispatcher.class);

    @Override
    public void notifyResidentTicketUpdated(UUID residentUserId, String ticketFolio, TicketStatus newStatus, String notes) {
        log.info("[PUSH-DISPATCH] Dispatched push notification to resident [{}]: Ticket [{}] changed to [{}] - Details: {}",
                residentUserId, ticketFolio, newStatus, notes);
    }
}
