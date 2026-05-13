package org.example.notificationservice.events;

import java.time.Instant;

public record EmployeeCreatedEvent(
        Long employeeId,
        String name,
        String email,
        Instant timestamp
) {}
