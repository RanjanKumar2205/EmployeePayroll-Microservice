package org.example.notificationservice.mappers;

import org.example.notificationservice.dtos.NotificationRequestDto;
import org.example.notificationservice.dtos.NotificationResponseDto;
import org.example.notificationservice.entities.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    /** Maps a request DTO to a new (unsaved) Notification entity. */
    public Notification toEntity(NotificationRequestDto dto) {
        return Notification.builder()
                .recipientId(dto.getRecipientId())
                .recipientEmail(dto.getRecipientEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .channel(dto.getChannel())
                .build();
    }

    /** Maps a persisted Notification entity to its response DTO. */
    public NotificationResponseDto toResponse(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setRecipientId(n.getRecipientId());
        dto.setRecipientEmail(n.getRecipientEmail());
        dto.setSubject(n.getSubject());
        dto.setMessage(n.getMessage());
        dto.setChannel(n.getChannel());
        dto.setStatus(n.getStatus());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setSentAt(n.getSentAt());
        return dto;
    }
}
