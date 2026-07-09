package edu.cit.amihan.medibook.auth.dto;

import edu.cit.amihan.medibook.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private Role role; // STAFF or DOCTOR only — validated in controller

    // Only used when role == DOCTOR or STAFF
    private String specialization;
    private String contactNumber;
    private String position;

    // Only used for patient self-registration
    private LocalDate dateOfBirth;
    private String address;
}