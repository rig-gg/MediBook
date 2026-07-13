package edu.cit.amihan.medibook.appointment.service;

import edu.cit.amihan.medibook.appointment.dto.AppointmentRequest;
import edu.cit.amihan.medibook.appointment.dto.AppointmentResponse;
import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.email.EmailService;
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
    private final EmailService emailService;

    @Transactional
    public AppointmentResponse bookAppointment(Long userId, AppointmentRequest request) {
        Patient patient = patientRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for this account."));

        DoctorSchedule schedule = scheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + request.getScheduleId()));

        if (!Boolean.TRUE.equals(schedule.getIsAvailable())) {
            throw new IllegalStateException("This time slot is no longer available.");
        }

        schedule.setIsAvailable(false);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .schedule(schedule)
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        AppointmentStatus oldStatus = appointment.getStatus();

        if (oldStatus == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointments cannot be modified.");
        }

        boolean valid = switch (oldStatus) {
            case PENDING   -> newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED;
            case CONFIRMED -> newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED;
            case CANCELLED -> false;
            default        -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Cannot transition appointment from " + oldStatus + " to " + newStatus + ".");
        }

        if (newStatus == AppointmentStatus.CANCELLED) {
            DoctorSchedule schedule = appointment.getSchedule();
            schedule.setIsAvailable(true);
        }

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);

        // FR-011: send email notification on status change
        emailService.sendStatusChangeEmail(saved, oldStatus, newStatus);

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long userId) {
        Patient patient = patientRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for this account."));

        return appointmentRepository.findByPatientPatientIdOrderByCreatedAtDesc(patient.getPatientId())
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(AppointmentStatus statusFilter) {
        List<Appointment> appointments = (statusFilter != null)
                ? appointmentRepository.findByStatusOrderByCreatedAtAsc(statusFilter)
                : appointmentRepository.findAllByOrderByCreatedAtDesc();

        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId, AppointmentStatus statusFilter) {
        List<Appointment> appointments = appointmentRepository
                .findByScheduleDoctorDoctorIdOrderByCreatedAtDesc(doctorId);

        if (statusFilter != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getStatus() == statusFilter)
                    .toList();
        }

        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }
}