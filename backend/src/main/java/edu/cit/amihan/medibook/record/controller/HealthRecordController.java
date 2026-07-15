package edu.cit.amihan.medibook.record.controller;

import edu.cit.amihan.medibook.record.dto.HealthRecordRequest;
import edu.cit.amihan.medibook.record.dto.HealthRecordResponse;
import edu.cit.amihan.medibook.record.service.HealthRecordService;
import edu.cit.amihan.medibook.user.entity.User;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

    private final HealthRecordService service;

    public HealthRecordController(HealthRecordService service) {
        this.service = service;
    }

    // FR-007 — only the assigned doctor can create a record for this appointment
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<HealthRecordResponse> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody HealthRecordRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createRecord(req, currentUser.getUserId()));
    }

    // FR-010 — fetch health record for a specific appointment (includes FDA suggestions)
    // PATIENT can only read their own; DOCTOR/STAFF can read any
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR','STAFF','PATIENT')")
    public HealthRecordResponse byAppointment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long appointmentId) {
        return service.getByAppointmentId(appointmentId, currentUser);
    }

    // FR-008 — DOCTOR can only see records of patients they are assigned to; STAFF can see all
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','STAFF')")
    public List<HealthRecordResponse> byPatient(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long patientId) {
        return service.getByPatient(patientId, currentUser);
    }

    // Doctors can update diagnosis and consultation notes
    @PutMapping("/{recordId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public HealthRecordResponse update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long recordId,
            @Valid @RequestBody HealthRecordRequest req) {
        return service.updateRecord(recordId, req, currentUser.getUserId());
    }
}
