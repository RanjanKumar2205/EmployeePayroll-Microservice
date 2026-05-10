package org.example.authservice.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private Long   id;
    private String username;
    private String role;
    private Boolean enabled;
}
