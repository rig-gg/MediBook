package edu.cit.amihan.medibook.doctor.dto;

import edu.cit.amihan.medibook.doctor.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private Long doctorId;
    private String fullName;
    private String specialization;
    private String contactNumber;
    private String email;

    public static DoctorResponse fromEntity(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .fullName(doctor.getFullName())
                .specialization(doctor.getSpecialization())
                .contactNumber(doctor.getContactNumber())
                .email(doctor.getUser().getEmail())
                .build();
    }
}