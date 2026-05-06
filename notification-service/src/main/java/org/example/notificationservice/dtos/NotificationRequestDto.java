package org.example.notificationservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.notificationservice.entities.Channel;

@Data
public class NotificationRequestDto {
    private Long recipientId;

    @Email @NotBlank
    private String recipientEmail;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    private Channel channel = Channel.EMAIL;
}
