package edu.cit.amihan.medibook.patient.dto;

import edu.cit.amihan.medibook.patient.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {

    private Long patientId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String contactNumber;
    private String address;
    private String email;

    public static PatientResponse fromEntity(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientId())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .contactNumber(patient.getContactNumber())
                .address(patient.getAddress())
                .email(patient.getUser().getEmail())
                .build();
    }
}
