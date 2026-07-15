package edu.cit.amihan.medibook.record.service;

import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.appointment.service.AppointmentService;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.fda.FdaService;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.record.dto.HealthRecordRequest;
import edu.cit.amihan.medibook.record.entity.HealthRecord;
import edu.cit.amihan.medibook.record.repository.HealthRecordRepository;
import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthRecordService — IDOR & Business Logic Tests")
class HealthRecordServiceTest {

    @Mock private HealthRecordRepository recordRepo;
    @Mock private AppointmentRepository appointmentRepo;
    @Mock private AppointmentService appointmentService;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private FdaService fdaService;

    @InjectMocks private HealthRecordService healthRecordService;

    private User doctorUser;
    private Doctor doctor;
    private User patientUser;
    private Patient patient;
    private DoctorSchedule schedule;
    private Appointment appointment;
    private HealthRecord record;

    @BeforeEach
    void setUp() {
        doctorUser = User.builder().userId(1L).username("doctor1").role(Role.DOCTOR).build();
        doctor = Doctor.builder().doctorId(10L).user(doctorUser).fullName("Dr. Santos").build();

        patientUser = User.builder().userId(2L).username("patient1").role(Role.PATIENT).build();
        patient = Patient.builder().patientId(20L).user(patientUser).fullName("Juan Dela Cruz").build();

        schedule = DoctorSchedule.builder()
                .scheduleId(30L).doctor(doctor)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        appointment = Appointment.builder()
                .appointmentId(100L).patient(patient).schedule(schedule)
                .status(AppointmentStatus.CONFIRMED).build();

        record = new HealthRecord();
        record.setRecordId(200L);
        record.setAppointment(appointment);
        record.setDoctor(doctor);
        record.setPatient(patient);
        record.setDiagnosis("Hypertension");
        record.setConsultationNotes("Follow up in 2 weeks");
        record.setRecordedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Create record — only assigned doctor can create")
    void createRecord_wrongDoctor_forbidden() {
        User otherDoctorUser = User.builder().userId(99L).role(Role.DOCTOR).build();
        HealthRecordRequest req = new HealthRecordRequest();
        req.setAppointmentId(100L);
        req.setDiagnosis("Test");

        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class, () ->
                healthRecordService.createRecord(req, otherDoctorUser.getUserId()));
    }

    @Test
    @DisplayName("Create record — rejects non-CONFIRMED appointments")
    void createRecord_notConfirmed() {
        appointment.setStatus(AppointmentStatus.PENDING);
        HealthRecordRequest req = new HealthRecordRequest();
        req.setAppointmentId(100L);
        req.setDiagnosis("Test");

        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(ResponseStatusException.class, () ->
                healthRecordService.createRecord(req, doctorUser.getUserId()));
    }

    @Test
    @DisplayName("Create record — rejects duplicate record for same appointment")
    void createRecord_duplicateRecord() {
        HealthRecordRequest req = new HealthRecordRequest();
        req.setAppointmentId(100L);
        req.setDiagnosis("Test");

        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appointment));
        when(recordRepo.existsByAppointmentAppointmentId(100L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                healthRecordService.createRecord(req, doctorUser.getUserId()));
    }

    @Test
    @DisplayName("Create record — success triggers appointment completion and FDA query")
    void createRecord_success() {
        HealthRecordRequest req = new HealthRecordRequest();
        req.setAppointmentId(100L);
        req.setDiagnosis("Hypertension");
        req.setConsultationNotes("Monitor blood pressure");

        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(appointment));
        when(recordRepo.existsByAppointmentAppointmentId(100L)).thenReturn(false);
        when(recordRepo.save(any())).thenAnswer(inv -> {
            HealthRecord r = inv.getArgument(0);
            r.setRecordId(200L);
            return r;
        });
        when(fdaService.getSuggestions("Hypertension")).thenReturn(List.of());

        healthRecordService.createRecord(req, doctorUser.getUserId());

        verify(appointmentService).updateStatus(100L, AppointmentStatus.COMPLETED);
        verify(fdaService).getSuggestions("Hypertension");
    }

    @Test
    @DisplayName("Get by appointment — patient can only read their own record")
    void getByAppointment_patientIdorCheck() {
        User otherPatientUser = User.builder().userId(50L).role(Role.PATIENT).build();
        Patient otherPatient = Patient.builder().patientId(60L).user(otherPatientUser).build();

        when(recordRepo.findByAppointmentAppointmentId(100L)).thenReturn(Optional.of(record));
        when(patientRepository.findByUserUserId(50L)).thenReturn(Optional.of(otherPatient));

        assertThrows(ResponseStatusException.class, () ->
                healthRecordService.getByAppointmentId(100L, otherPatientUser));
    }

    @Test
    @DisplayName("Get by appointment — patient can read their own record")
    void getByAppointment_patientOwnRecord() {
        when(recordRepo.findByAppointmentAppointmentId(100L)).thenReturn(Optional.of(record));
        when(patientRepository.findByUserUserId(2L)).thenReturn(Optional.of(patient));
        when(fdaService.getSuggestions(anyString())).thenReturn(List.of());

        var response = healthRecordService.getByAppointmentId(100L, patientUser);

        assertEquals("Hypertension", response.getDiagnosis());
    }

    @Test
    @DisplayName("Get by patient — doctor can only see records of patients they have appointments with")
    void getByPatient_doctorIdorCheck() {
        User otherDoctorUser = User.builder().userId(70L).role(Role.DOCTOR).build();

        when(doctorRepository.findByUserUserId(70L)).thenReturn(Optional.of(
                Doctor.builder().doctorId(80L).user(otherDoctorUser).build()));
        when(appointmentRepo.existsByScheduleDoctorDoctorIdAndPatientPatientId(80L, 20L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                healthRecordService.getByPatient(20L, otherDoctorUser));
    }

    @Test
    @DisplayName("Get by patient — doctor with appointment can see records")
    void getByPatient_doctorWithAppointment() {
        when(doctorRepository.findByUserUserId(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepo.existsByScheduleDoctorDoctorIdAndPatientPatientId(10L, 20L)).thenReturn(true);
        when(recordRepo.findByPatientPatientId(20L)).thenReturn(List.of(record));

        var results = healthRecordService.getByPatient(20L, doctorUser);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Update record — only the owning doctor can update")
    void updateRecord_wrongDoctor_forbidden() {
        User otherDoctorUser = User.builder().userId(99L).role(Role.DOCTOR).build();
        HealthRecordRequest req = new HealthRecordRequest();
        req.setDiagnosis("Updated");

        when(recordRepo.findById(200L)).thenReturn(Optional.of(record));

        assertThrows(ResponseStatusException.class, () ->
                healthRecordService.updateRecord(200L, req, otherDoctorUser.getUserId()));
    }

    @Test
    @DisplayName("Update record — owning doctor can update diagnosis")
    void updateRecord_ownDoctor_success() {
        HealthRecordRequest req = new HealthRecordRequest();
        req.setDiagnosis("Updated Hypertension");
        req.setConsultationNotes("New notes");

        when(recordRepo.findById(200L)).thenReturn(Optional.of(record));
        when(recordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = healthRecordService.updateRecord(200L, req, doctorUser.getUserId());

        assertEquals("Updated Hypertension", response.getDiagnosis());
    }
}
