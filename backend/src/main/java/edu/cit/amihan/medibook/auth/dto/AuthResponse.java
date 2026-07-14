package edu.cit.amihan.medibook.auth.dto;

import edu.cit.amihan.medibook.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String fullName;
    private Role role;
}
