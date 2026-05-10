package org.example.authservice.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long   expiresInMs;
    private String username;
    private String role;
}
