package org.example.authservice.mappers;

import org.example.authservice.dtos.AuthResponseDto;
import org.example.authservice.dtos.RegisterRequestDto;
import org.example.authservice.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Value("${jwt.expiration-ms}")
    private long expirationInMs;

    public AuthMapper(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User toEntity(RegisterRequestDto dto) {
        return User.builder()
                .username(dto.getUsername())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .build();
    }

    public AuthResponseDto toResponse(User user) {
        return AuthResponseDto.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponseDto toResponse(User user, String token) {
        return AuthResponseDto.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(token)
                .expiresInMs(expirationInMs)
                .build();
    }
}
