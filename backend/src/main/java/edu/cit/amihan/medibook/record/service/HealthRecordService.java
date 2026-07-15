package edu.cit.amihan.medibook.record.service;

import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.appointment.service.AppointmentService;
import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.fda.FdaDrugSuggestion;
import edu.cit.amihan.medibook.fda.FdaService;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.record.dto.HealthRecordRequest;
import edu.cit.amihan.medibook.record.dto.HealthRecordResponse;
import edu.cit.amihan.medibook.record.entity.HealthRecord;
import edu.cit.amihan.medibook.record.repository.HealthRecordRepository;
import edu.cit.amihan.medibook.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository recordRepo;
    private final AppointmentRepository appointmentRepo;
    @Lazy
    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final FdaService fdaService;

    @Transactional
    public HealthRecordResponse createRecord(HealthRecordRequest req, Long currentUserId) {
        Appointment appt = appointmentRepo.findById(req.getAppointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        Long assignedDoctorUserId = appt.getSchedule().getDoctor().getUser().getUserId();
        if (!Objects.equals(assignedDoctorUserId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not the doctor assigned to this appointment.");
        }

        if (appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only CONFIRMED appointments can be completed with a record");
        }

        if (recordRepo.existsByAppointmentAppointmentId(appt.getAppointmentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A health record already exists for this appointment");
        }

        HealthRecord rec = new HealthRecord();
        rec.setAppointment(appt);
        rec.setDoctor(appt.getSchedule().getDoctor());
        rec.setPatient(appt.getPatient());
        rec.setDiagnosis(req.getDiagnosis());
        rec.setConsultationNotes(req.getConsultationNotes());

        HealthRecord saved = recordRepo.save(rec);

        // Delegate status transition through AppointmentService for proper state machine + email notification
        appointmentService.updateStatus(appt.getAppointmentId(), AppointmentStatus.COMPLETED);

        HealthRecordResponse response = HealthRecordResponse.fromEntity(saved);

        // FR-010: query OpenFDA for drug classification suggestions
        List<FdaDrugSuggestion> suggestions = fdaService.getSuggestions(req.getDiagnosis());
        response.setFdaSuggestions(suggestions);

        return response;
    }

    @Transactional(readOnly = true)
    public HealthRecordResponse getByAppointmentId(Long appointmentId, User currentUser) {
        HealthRecord rec = recordRepo.findByAppointmentAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No health record found for appointment id: " + appointmentId));

        // IDOR check: PATIENT can only read their own appointment's record
        if ("PATIENT".equals(currentUser.getRole().name())) {
            Patient patient = patientRepository.findByUserUserId(currentUser.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));
            if (!rec.getPatient().getPatientId().equals(patient.getPatientId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not have access to this health record.");
            }
        }

        HealthRecordResponse response = HealthRecordResponse.fromEntity(rec);

        // FR-010: re-query OpenFDA with the stored diagnosis so suggestions are always available
        List<FdaDrugSuggestion> suggestions = fdaService.getSuggestions(rec.getDiagnosis());
        response.setFdaSuggestions(suggestions);

        return response;
    }

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getByPatient(Long patientId, User currentUser) {
        // IDOR check: DOCTOR can only see records of patients they are assigned to
        if ("DOCTOR".equals(currentUser.getRole().name())) {
            Doctor doctor = doctorRepository.findByUserUserId(currentUser.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));
            boolean hasAppointment = appointmentRepo.existsByScheduleDoctorDoctorIdAndPatientPatientId(
                    doctor.getDoctorId(), patientId);
            if (!hasAppointment) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not assigned to this patient.");
            }
        }

        return recordRepo.findByPatientPatientId(patientId)
                .stream()
                .map(HealthRecordResponse::fromEntity)
                .toList();
    }

    @Transactional
    public HealthRecordResponse updateRecord(Long recordId, HealthRecordRequest req, Long currentUserId) {
        HealthRecord rec = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health record not found with id: " + recordId));

        Long recordDoctorUserId = rec.getDoctor().getUser().getUserId();
        if (!Objects.equals(recordDoctorUserId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only update your own records.");
        }

        if (req.getDiagnosis() != null) {
            rec.setDiagnosis(req.getDiagnosis());
        }
        if (req.getConsultationNotes() != null) {
            rec.setConsultationNotes(req.getConsultationNotes());
        }

        return HealthRecordResponse.fromEntity(recordRepo.save(rec));
    }
}