package edu.cit.amihan.medibook.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {

    @NotNull(message = "scheduleId is required")
    private Long scheduleId;
}