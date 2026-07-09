package edu.cit.amihan.medibook.doctor.dto;

import lombok.Data;

@Data
public class DoctorRequest {
    private String fullName;
    private String specialization;
    private String contactNumber;
}
