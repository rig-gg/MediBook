package edu.cit.amihan.medibook.patient.controller;

import edu.cit.amihan.medibook.patient.dto.PatientRequest;
import edu.cit.amihan.medibook.patient.dto.PatientResponse;
import edu.cit.amihan.medibook.patient.service.PatientService;
import edu.cit.amihan.medibook.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    public List<PatientResponse> getPatients(
            @RequestParam(required = false) String search) {
        return service.getAllPatients(search);
    }

    @GetMapping("/{patientId}")
    public PatientResponse getPatientById(@PathVariable Long patientId) {
        return service.getPatientById(patientId);
    }

    @GetMapping("/me")
    public PatientResponse getMyProfile(@AuthenticationPrincipal User currentUser) {
        return service.getPatientByUserId(currentUser.getUserId());
    }

    @PutMapping("/me")
    public ResponseEntity<PatientResponse> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(service.updatePatient(currentUser.getUserId(), request));
    }

    @DeleteMapping("/{patientId}")
    public void deletePatient(@PathVariable Long patientId) {
        service.deletePatient(patientId);
    }
}
