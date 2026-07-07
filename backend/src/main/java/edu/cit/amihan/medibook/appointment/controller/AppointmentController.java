package edu.cit.amihan.medibook.appointment.controller;

import edu.cit.amihan.medibook.appointment.dto.AppointmentRequest;
import edu.cit.amihan.medibook.appointment.dto.AppointmentResponse;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.service.AppointmentService;
import edu.cit.amihan.medibook.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // PATIENT — book an appointment against an available schedule (FR-003)
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(
                appointmentService.bookAppointment(currentUser.getUserId(), request));
    }

    // PATIENT — view own appointment history
    @GetMapping("/me")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(currentUser.getUserId()));
    }

    // STAFF/DOCTOR/ADMIN — view all appointments, optionally filtered by status
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @RequestParam(required = false) AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(status));
    }

    // STAFF — approve, cancel, or modify status (FR-004)
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
}