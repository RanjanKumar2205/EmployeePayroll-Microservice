package org.example.authservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.authservice.entities.Role;

@Data
public class RoleUpdateDto {
    @NotBlank(message = "Username is required")
    @Email(message = "Email must be a valid address")
    private String username;

    @NotNull(message = "Role is required")
    private Role role;
}
