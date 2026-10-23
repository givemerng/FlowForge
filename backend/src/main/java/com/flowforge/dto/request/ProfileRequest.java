package com.flowforge.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email
) {
}
