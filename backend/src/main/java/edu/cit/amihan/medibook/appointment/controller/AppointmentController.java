package edu.cit.amihan.medibook.appointment.controller;

import edu.cit.amihan.medibook.appointment.dto.AppointmentRequest;
import edu.cit.amihan.medibook.appointment.dto.AppointmentResponse;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.service.AppointmentService;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;

    // PATIENT — book an appointment against an available schedule (FR-003)
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.bookAppointment(currentUser.getUserId(), request));
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
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) AppointmentStatus status) {
        // PATIENT should not reach here thanks to SecurityConfig,
        // but guard defensively anyway
        if (currentUser.getRole().name().equals("PATIENT")) {
            return ResponseEntity.ok(appointmentService.getMyAppointments(currentUser.getUserId()));
        }
        return ResponseEntity.ok(appointmentService.getAllAppointments(status));
    }

    // DOCTOR — view their own appointment queue, optionally filtered by status
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long doctorId,
            @RequestParam(required = false) AppointmentStatus status) {
        // DOCTOR role can only see their own appointments
        if (currentUser.getRole().name().equals("DOCTOR")) {
            Long myDoctorId = doctorRepository.findByUserUserId(currentUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"))
                    .getDoctorId();
            if (!myDoctorId.equals(doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId, status));
    }

    // STAFF — approve, cancel, or modify status (FR-004)
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }
}