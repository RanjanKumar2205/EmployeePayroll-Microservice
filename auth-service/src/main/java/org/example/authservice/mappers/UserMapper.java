package org.example.authservice.mappers;

import org.example.authservice.dtos.UserResponseDto;
import org.example.authservice.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDto toResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .build();
    }
}
