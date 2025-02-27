package org.ithub.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordCheckRequest {
    @NotBlank(message = "Password is required")
    private String rawPassword;

    public PasswordCheckRequest(String rawPassword) {
        this.rawPassword = rawPassword;
    }
}

