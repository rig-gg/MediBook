package edu.cit.amihan.medibook.clinicstaff.dto;

import edu.cit.amihan.medibook.clinicstaff.entity.ClinicStaff;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicStaffResponse {

    private Long staffId;
    private String fullName;
    private String contactNumber;
    private String position;
    private String email;

    public static ClinicStaffResponse fromEntity(ClinicStaff staff) {
        return ClinicStaffResponse.builder()
                .staffId(staff.getStaffId())
                .fullName(staff.getFullName())
                .contactNumber(staff.getContactNumber())
                .position(staff.getPosition())
                .email(staff.getUser().getEmail())
                .build();
    }
}
