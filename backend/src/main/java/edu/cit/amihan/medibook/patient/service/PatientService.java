package edu.cit.amihan.medibook.patient.service;

import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.patient.dto.PatientRequest;
import edu.cit.amihan.medibook.patient.dto.PatientResponse;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.record.repository.HealthRecordRepository;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          HealthRecordRepository healthRecordRepository,
                          UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public PatientResponse getPatientByUserId(Long userId) {
        var patient = patientRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));
        return PatientResponse.fromEntity(patient);
    }

    @Transactional
    public PatientResponse updatePatient(Long userId, PatientRequest request) {
        Patient patient = patientRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        patient.setFullName(request.getFullName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setContactNumber(request.getContactNumber());
        patient.setAddress(request.getAddress());

        return PatientResponse.fromEntity(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        if (appointmentRepository.existsByPatientPatientId(patientId)) {
            throw new IllegalStateException("Cannot delete patient with existing appointments.");
        }
        if (healthRecordRepository.existsByPatientPatientId(patientId)) {
            throw new IllegalStateException("Cannot delete patient with existing health records.");
        }

        User user = patient.getUser();
        Long userId = user.getUserId();
        patientRepository.delete(patient);
        entityManager.flush();
        userRepository.deleteById(userId);
    }
}
