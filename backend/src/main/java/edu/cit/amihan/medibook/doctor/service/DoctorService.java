package edu.cit.amihan.medibook.doctor.service;

import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.dto.DoctorRequest;
import edu.cit.amihan.medibook.doctor.dto.DoctorResponse;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors(String specialization) {
        List<Doctor> doctors = (specialization != null && !specialization.isBlank())
                ? doctorRepository.findBySpecializationContainingIgnoreCase(specialization)
                : doctorRepository.findAll();

        return doctors.stream()
                .map(DoctorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        return DoctorResponse.fromEntity(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));

        return DoctorResponse.fromEntity(doctor);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long doctorId, DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (request.getFullName() != null) {
            doctor.setFullName(request.getFullName());
        }
        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }
        if (request.getContactNumber() != null) {
            doctor.setContactNumber(request.getContactNumber());
        }

        return DoctorResponse.fromEntity(doctorRepository.save(doctor));
    }
}