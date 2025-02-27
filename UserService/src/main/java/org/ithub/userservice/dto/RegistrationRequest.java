package org.ithub.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationRequest {
    private String email;
    private String username;
    private String password;

    public RegistrationRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public UserDto toUserDto() {
        return new UserDto(null, username, email);
    }
}
