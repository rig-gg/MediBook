package edu.cit.amihan.medibook.clinicstaff.controller;

import edu.cit.amihan.medibook.clinicstaff.dto.ClinicStaffRequest;
import edu.cit.amihan.medibook.clinicstaff.dto.ClinicStaffResponse;
import edu.cit.amihan.medibook.clinicstaff.service.ClinicStaffService;

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
@RequestMapping("/api/staff")
public class ClinicStaffController {

    private final ClinicStaffService service;

    public ClinicStaffController(ClinicStaffService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClinicStaffResponse> getStaff(
            @RequestParam(required = false) String search) {
        return service.getAllStaff(search);
    }

    @GetMapping("/{staffId}")
    public ClinicStaffResponse getStaffById(@PathVariable Long staffId) {
        return service.getStaffById(staffId);
    }

    @GetMapping("/user/{userId}")
    public ClinicStaffResponse getStaffByUserId(@PathVariable Long userId) {
        return service.getStaffByUserId(userId);
    }

    @PutMapping("/{staffId}")
    public ClinicStaffResponse updateStaff(@PathVariable Long staffId,
            @RequestBody ClinicStaffRequest request) {
        return service.updateStaff(staffId, request);
    }

    @DeleteMapping("/{staffId}")
    public void deleteStaff(@PathVariable Long staffId) {
        service.deleteStaff(staffId);
    }
}
