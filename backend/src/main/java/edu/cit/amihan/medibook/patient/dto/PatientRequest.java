package edu.cit.amihan.medibook.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private LocalDate dateOfBirth;

    private String contactNumber;

    private String address;
}
