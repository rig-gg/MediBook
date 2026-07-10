package edu.cit.amihan.medibook.clinicstaff.service;

import edu.cit.amihan.medibook.clinicstaff.dto.ClinicStaffRequest;
import edu.cit.amihan.medibook.clinicstaff.dto.ClinicStaffResponse;
import edu.cit.amihan.medibook.clinicstaff.entity.ClinicStaff;
import edu.cit.amihan.medibook.clinicstaff.repository.ClinicStaffRepository;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ClinicStaffService {

    private final ClinicStaffRepository repository;

    public ClinicStaffService(ClinicStaffRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ClinicStaffResponse> getAllStaff(String search) {
        List<ClinicStaff> staffList;
        if (search != null && !search.isBlank()) {
            staffList = repository.searchStaff(search.trim());
        } else {
            staffList = repository.findAllWithUser();
        }
        return staffList.stream()
                .map(ClinicStaffResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClinicStaffResponse getStaffById(Long staffId) {
        ClinicStaff staff = repository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        return ClinicStaffResponse.fromEntity(staff);
    }

    @Transactional(readOnly = true)
    public ClinicStaffResponse getStaffByUserId(Long userId) {
        ClinicStaff staff = repository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found for user id: " + userId));
        return ClinicStaffResponse.fromEntity(staff);
    }

    @Transactional
    public ClinicStaffResponse updateStaff(Long staffId, ClinicStaffRequest request) {
        ClinicStaff staff = repository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        if (request.getFullName() != null) {
            staff.setFullName(request.getFullName());
        }
        if (request.getContactNumber() != null) {
            staff.setContactNumber(request.getContactNumber());
        }
        if (request.getPosition() != null) {
            staff.setPosition(request.getPosition());
        }

        return ClinicStaffResponse.fromEntity(repository.save(staff));
    }
}
