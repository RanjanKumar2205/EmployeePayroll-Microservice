package org.example.notificationservice.mappers;

import org.example.notificationservice.dtos.NotificationRequestDto;
import org.example.notificationservice.dtos.NotificationResponseDto;
import org.example.notificationservice.entities.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequestDto dto) {
        return Notification.builder()
                .recipientId(dto.getRecipientId())
                .recipientEmail(dto.getRecipientEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .channel(dto.getChannel())
                .build();
    }

    public NotificationResponseDto toResponse(Notification n) {
        return NotificationResponseDto.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .recipientEmail(n.getRecipientEmail())
                .subject(n.getSubject())
                .message(n.getMessage())
                .channel(n.getChannel())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}
