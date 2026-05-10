package org.example.authservice.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDto {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String error;
    private String path;
}
