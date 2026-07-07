package edu.cit.amihan.medibook.schedule.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorScheduleRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotNull(message = "startTime is required")
    @Future(message = "startTime must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    @Future(message = "endTime must be in the future")
    private LocalDateTime endTime;
}