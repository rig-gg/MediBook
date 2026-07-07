package edu.cit.amihan.medibook.appointment.service;

import edu.cit.amihan.medibook.appointment.dto.AppointmentRequest;
import edu.cit.amihan.medibook.appointment.dto.AppointmentResponse;
import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import edu.cit.amihan.medibook.schedule.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;

    // @Transactional is required here — the pessimistic lock acquired by
    // findByIdForUpdate() is only held for the duration of this transaction.
    @Transactional
    public AppointmentResponse bookAppointment(Long userId, AppointmentRequest request) {
        Patient patient = patientRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for this account."));

        // Row-level lock: any other concurrent booking attempt on this exact
        // schedule_id blocks here until this transaction commits or rolls back.
        DoctorSchedule schedule = scheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + request.getScheduleId()));

        // FR-006 / BR-003: reject if another request already claimed this slot
        if (!Boolean.TRUE.equals(schedule.getIsAvailable())) {
            throw new IllegalStateException("This time slot is no longer available.");
        }

        schedule.setIsAvailable(false);
        scheduleRepository.save(schedule);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .schedule(schedule)
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    // FR-004: staff approves, cancels, or modifies appointment status
    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        // BR-004: completed + recorded appointments are immutable
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointments cannot be modified.");
        }

        // If cancelling, free the slot back up for rebooking
        if (newStatus == AppointmentStatus.CANCELLED) {
            DoctorSchedule schedule = appointment.getSchedule();
            schedule.setIsAvailable(true);
            scheduleRepository.save(schedule);
        }

        appointment.setStatus(newStatus);
        return AppointmentResponse.fromEntity(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getMyAppointments(Long userId) {
        Patient patient = patientRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for this account."));

        return appointmentRepository.findByPatientPatientIdOrderByCreatedAtDesc(patient.getPatientId())
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    public List<AppointmentResponse> getAllAppointments(AppointmentStatus statusFilter) {
        List<Appointment> appointments = (statusFilter != null)
                ? appointmentRepository.findByStatusOrderByCreatedAtAsc(statusFilter)
                : appointmentRepository.findAllByOrderByCreatedAtDesc();

        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }
}