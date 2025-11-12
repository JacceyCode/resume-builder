package com.jaccey.resumebuilderapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters.")
    private String name;

    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters.")
    private String password;

    private String profileImageUrl;
}
