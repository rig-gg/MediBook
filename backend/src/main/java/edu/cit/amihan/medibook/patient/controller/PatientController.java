package edu.cit.amihan.medibook.patient.controller;

import edu.cit.amihan.medibook.patient.dto.PatientResponse;
import edu.cit.amihan.medibook.patient.service.PatientService;
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
}
