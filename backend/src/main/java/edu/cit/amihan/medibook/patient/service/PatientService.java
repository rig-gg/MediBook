package edu.cit.amihan.medibook.patient.service;

import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.patient.dto.PatientResponse;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients(String search) {
        List<PatientResponse> patients;
        if (search != null && !search.isBlank()) {
            patients = patientRepository.searchPatients(search.trim())
                    .stream().map(PatientResponse::fromEntity).toList();
        } else {
            patients = patientRepository.findAllWithUser()
                    .stream().map(PatientResponse::fromEntity).toList();
        }
        return patients;
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long patientId) {
        var patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return PatientResponse.fromEntity(patient);
    }
}
