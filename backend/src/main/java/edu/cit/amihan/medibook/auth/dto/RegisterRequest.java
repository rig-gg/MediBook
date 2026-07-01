package edu.cit.amihan.medibook.auth.dto;

import edu.cit.amihan.medibook.user.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Role role; // STAFF or DOCTOR only — validated in controller

    // Only used when role == DOCTOR
    private String specialization;
    private String contactNumber;
}