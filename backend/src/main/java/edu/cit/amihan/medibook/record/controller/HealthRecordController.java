package edu.cit.amihan.medibook.record.controller;

import edu.cit.amihan.medibook.record.dto.HealthRecordRequest;
import edu.cit.amihan.medibook.record.dto.HealthRecordResponse;
import edu.cit.amihan.medibook.record.service.HealthRecordService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

    private final HealthRecordService service;

    public HealthRecordController(HealthRecordService service) {
        this.service = service;
    }

    // FR-007
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<HealthRecordResponse> create(@RequestBody HealthRecordRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRecord(req));
    }

    // FR-008
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR','STAFF')")
    public List<HealthRecordResponse> byPatient(@PathVariable Long patientId) {
        return service.getByPatient(patientId);
    }
}
