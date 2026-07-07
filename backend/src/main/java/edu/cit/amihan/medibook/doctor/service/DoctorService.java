package edu.cit.amihan.medibook.doctor.service;

import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.dto.DoctorResponse;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<DoctorResponse> getAllDoctors(String specialization) {
        List<Doctor> doctors;

        if (specialization != null && !specialization.isBlank()) {
            doctors = doctorRepository.findBySpecializationContainingIgnoreCase(specialization.trim());
        } else {
            doctors = doctorRepository.findAll();
        }

        return doctors.stream()
                .map(DoctorResponse::fromEntity)
                .toList();
    }

    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        return DoctorResponse.fromEntity(doctor);
    }
}