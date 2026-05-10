package org.example.authservice.services;

import org.example.authservice.dtos.RoleUpdateDto;
import org.example.authservice.dtos.UserResponseDto;
import org.example.authservice.entities.User;
import org.example.authservice.exceptions.ResourceNotFoundException;
import org.example.authservice.mappers.UserMapper;
import org.example.authservice.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponseDto updateRole(RoleUpdateDto dto, Authentication auth) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getIsProtected()) {
            throw new IllegalArgumentException("This account is protected and cannot be modified");
        }
        if (user.getUsername().equals(auth.getName())) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        user.setRole(dto.getRole());
        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
