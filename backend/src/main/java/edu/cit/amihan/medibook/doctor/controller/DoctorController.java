package edu.cit.amihan.medibook.doctor.controller;

import edu.cit.amihan.medibook.doctor.dto.DoctorRequest;
import edu.cit.amihan.medibook.doctor.dto.DoctorResponse;
import edu.cit.amihan.medibook.doctor.service.DoctorService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    @GetMapping
    public List<DoctorResponse> getDoctors(
            @RequestParam(required = false) String specialization) {
        return service.getAllDoctors(specialization);
    }

    @GetMapping("/{doctorId}")
    public DoctorResponse getDoctorById(@PathVariable Long doctorId) {
        return service.getDoctorById(doctorId);
    }

    @GetMapping("/user/{userId}")
    public DoctorResponse getDoctorByUserId(@PathVariable Long userId) {
        return service.getDoctorByUserId(userId);
    }

    @PutMapping("/{doctorId}")
    public DoctorResponse updateDoctor(@PathVariable Long doctorId,
            @RequestBody DoctorRequest request) {
        return service.updateDoctor(doctorId, request);
    }

    @DeleteMapping("/{doctorId}")
    public void deleteDoctor(@PathVariable Long doctorId) {
        service.deleteDoctor(doctorId);
    }
}