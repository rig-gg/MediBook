package edu.cit.amihan.medibook.appointment.service;

import edu.cit.amihan.medibook.appointment.dto.AppointmentResponse;
import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.email.EmailService;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.record.repository.HealthRecordRepository;
import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import edu.cit.amihan.medibook.schedule.repository.DoctorScheduleRepository;
import edu.cit.amihan.medibook.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService — State Machine Tests")
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private DoctorScheduleRepository scheduleRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private EmailService emailService;
    @Mock private HealthRecordRepository healthRecordRepository;

    @InjectMocks private AppointmentService appointmentService;

    private User user;
    private Patient patient;
    private Doctor doctor;
    private DoctorSchedule schedule;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).username("patient1").role(edu.cit.amihan.medibook.user.entity.Role.PATIENT).build();
        patient = Patient.builder().patientId(10L).user(user).fullName("Juan Dela Cruz").build();
        doctor = Doctor.builder().doctorId(20L).user(User.builder().userId(2L).build()).fullName("Dr. Santos").build();
        schedule = DoctorSchedule.builder()
                .scheduleId(30L).doctor(doctor)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .isAvailable(true).build();
        appointment = Appointment.builder()
                .appointmentId(100L).patient(patient).schedule(schedule)
                .status(AppointmentStatus.PENDING).build();
    }

    @Test
    @DisplayName("Book appointment — marks slot as unavailable")
    void bookAppointment_success() {
        when(patientRepository.findByUserUserId(1L)).thenReturn(Optional.of(patient));
        when(scheduleRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.bookAppointment(1L,
                new edu.cit.amihan.medibook.appointment.dto.AppointmentRequest() {{ setScheduleId(30L); }});

        assertFalse(schedule.getIsAvailable(), "Slot should be marked unavailable after booking");
        verify(appointmentRepository).save(any());
    }

    @Test
    @DisplayName("Book appointment — throws if slot already taken")
    void bookAppointment_slotUnavailable() {
        schedule.setIsAvailable(false);
        when(patientRepository.findByUserUserId(1L)).thenReturn(Optional.of(patient));
        when(scheduleRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(schedule));

        assertThrows(IllegalStateException.class, () ->
                appointmentService.bookAppointment(1L,
                        new edu.cit.amihan.medibook.appointment.dto.AppointmentRequest() {{ setScheduleId(30L); }}));
    }

    @Test
    @DisplayName("Book appointment — throws if patient profile not found")
    void bookAppointment_patientNotFound() {
        when(patientRepository.findByUserUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                appointmentService.bookAppointment(99L,
                        new edu.cit.amihan.medibook.appointment.dto.AppointmentRequest() {{ setScheduleId(30L); }}));
    }

    // --- State machine transitions ---

    @Test
    @DisplayName("PENDING → CONFIRMED: valid transition")
    void updateStatus_pendingToConfirmed() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.updateStatus(100L, AppointmentStatus.CONFIRMED);

        assertEquals("CONFIRMED", response.getStatus());
        verify(emailService).sendStatusChangeEmail(any(), eq(AppointmentStatus.PENDING), eq(AppointmentStatus.CONFIRMED));
    }

    @Test
    @DisplayName("PENDING → CANCELLED: valid transition, reopens slot")
    void updateStatus_pendingToCancelled() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        appointmentService.updateStatus(100L, AppointmentStatus.CANCELLED);

        assertTrue(schedule.getIsAvailable(), "Slot should be available again after cancellation");
    }

    @Test
    @DisplayName("CONFIRMED → COMPLETED: valid transition")
    void updateStatus_confirmedToCompleted() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.updateStatus(100L, AppointmentStatus.COMPLETED);

        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    @DisplayName("CONFIRMED → CANCELLED: valid transition, reopens slot")
    void updateStatus_confirmedToCancelled() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        appointmentService.updateStatus(100L, AppointmentStatus.CANCELLED);

        assertTrue(schedule.getIsAvailable());
    }

    @Test
    @DisplayName("PENDING → COMPLETED: invalid transition (must go through CONFIRMED)")
    void updateStatus_pendingToCompleted_invalid() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () ->
                appointmentService.updateStatus(100L, AppointmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("COMPLETED → any: invalid (completed appointments are immutable)")
    void updateStatus_completedIsImmutable() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () ->
                appointmentService.updateStatus(100L, AppointmentStatus.CANCELLED));
    }

    @Test
    @DisplayName("CANCELLED → any: invalid (cancelled is a terminal state)")
    void updateStatus_cancelledIsTerminal() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class, () ->
                appointmentService.updateStatus(100L, AppointmentStatus.CONFIRMED));
    }

    // --- Delete ---

    @Test
    @DisplayName("Delete appointment — reopens slot")
    void deleteAppointment_success() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(healthRecordRepository.existsByAppointmentAppointmentId(100L)).thenReturn(false);

        appointmentService.deleteAppointment(100L);

        assertTrue(schedule.getIsAvailable());
        verify(appointmentRepository).delete(appointment);
    }

    @Test
    @DisplayName("Delete appointment — blocked if health record exists")
    void deleteAppointment_hasRecord() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(healthRecordRepository.existsByAppointmentAppointmentId(100L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                appointmentService.deleteAppointment(100L));

        verify(appointmentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete appointment — throws if not found")
    void deleteAppointment_notFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                appointmentService.deleteAppointment(999L));
    }
}
