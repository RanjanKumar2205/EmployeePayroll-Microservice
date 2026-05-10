package org.example.authservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.authservice.dtos.AuthResponseDto;
import org.example.authservice.dtos.LoginRequestDto;
import org.example.authservice.dtos.RegisterRequestDto;
import org.example.authservice.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, and user management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user and receive a JWT")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(requestDto));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }
}
