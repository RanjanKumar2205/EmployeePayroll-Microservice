package org.example.authservice.controllers;

import org.example.authservice.dtos.RoleUpdateDto;
import org.example.authservice.dtos.UserResponseDto;
import org.example.authservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/upgrade")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateRole(@RequestBody RoleUpdateDto dto,
                                        Authentication auth) {
        UserResponseDto responseDto = userService.updateRole(dto, auth);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
