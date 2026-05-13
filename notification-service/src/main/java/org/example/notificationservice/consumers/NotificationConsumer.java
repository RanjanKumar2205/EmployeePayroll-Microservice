package org.example.notificationservice.consumers;

import org.example.notificationservice.events.EmployeeCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "employee-created", groupId = "notification-group")
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("Welcome email would be sent to: {}", event.email());
    }
}
