package edu.cit.amihan.medibook.schedule.dto;

import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorScheduleResponse {

    private Long scheduleId;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAvailable;

    public static DoctorScheduleResponse fromEntity(DoctorSchedule schedule) {
        return DoctorScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .doctorId(schedule.getDoctor().getDoctorId())
                .doctorName(schedule.getDoctor().getFullName())
                .specialization(schedule.getDoctor().getSpecialization())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .isAvailable(schedule.getIsAvailable())
                .build();
    }
}