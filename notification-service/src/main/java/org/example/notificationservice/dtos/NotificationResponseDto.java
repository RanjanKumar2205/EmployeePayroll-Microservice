package org.example.notificationservice.dtos;

import lombok.Builder;
import lombok.Data;
import org.example.notificationservice.entities.Channel;
import org.example.notificationservice.entities.NotificationStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDto {
    private Long id;
    private Long recipientId;
    private String recipientEmail;
    private String subject;
    private String message;
    private Channel channel;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
