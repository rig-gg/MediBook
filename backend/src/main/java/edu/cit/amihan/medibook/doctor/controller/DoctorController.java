package edu.cit.amihan.medibook.doctor.controller;

import edu.cit.amihan.medibook.doctor.dto.DoctorResponse;
import edu.cit.amihan.medibook.doctor.service.DoctorService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<DoctorResponse> getDoctors() {
        return service.getAllDoctors();
    }
}