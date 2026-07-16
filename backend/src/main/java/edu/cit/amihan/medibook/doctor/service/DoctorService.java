package edu.cit.amihan.medibook.doctor.service;

import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.dto.DoctorRequest;
import edu.cit.amihan.medibook.doctor.dto.DoctorResponse;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.record.repository.HealthRecordRepository;
import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import edu.cit.amihan.medibook.schedule.repository.DoctorScheduleRepository;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DoctorService(DoctorRepository doctorRepository,
                         DoctorScheduleRepository scheduleRepository,
                         HealthRecordRepository healthRecordRepository,
                         AppointmentRepository appointmentRepository,
                         UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (healthRecordRepository.existsByDoctorDoctorId(doctorId)) {
            throw new IllegalStateException("Cannot delete doctor with existing health records.");
        }
        if (appointmentRepository.existsByScheduleDoctorDoctorId(doctorId)) {
            throw new IllegalStateException("Cannot delete doctor with existing appointments.");
        }

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorDoctorId(doctorId);
        if (!schedules.isEmpty()) {
            scheduleRepository.deleteAll(schedules);
        }

        User user = doctor.getUser();
        Long userId = user.getUserId();
        doctorRepository.delete(doctor);
        entityManager.flush();
        userRepository.deleteById(userId);
    }
}