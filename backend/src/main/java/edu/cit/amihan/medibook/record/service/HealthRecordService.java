package edu.cit.amihan.medibook.record.service;

import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.record.entity.HealthRecord;
import edu.cit.amihan.medibook.record.repository.HealthRecordRepository;
import edu.cit.amihan.medibook.record.dto.HealthRecordRequest;
import edu.cit.amihan.medibook.record.dto.HealthRecordResponse;

import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.repository.AppointmentRepository;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class HealthRecordService {

    private final HealthRecordRepository recordRepo;
    private final AppointmentRepository appointmentRepo;

    public HealthRecordService(HealthRecordRepository recordRepo,
                               AppointmentRepository appointmentRepo) {
        this.recordRepo = recordRepo;
        this.appointmentRepo = appointmentRepo;
    }

    // FR-007: doctor writes a record on completing an appointment.
    // BR-001: appointment transitions to COMPLETED via doctor.
    // BR-004: exactly one immutable record per appointment (no update/delete anywhere).
    @Transactional
    public HealthRecordResponse createRecord(HealthRecordRequest req, Long currentUserId) {
        Appointment appt = appointmentRepo.findById(req.getAppointmentId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Appointment not found"));

        // Ownership check: only the assigned doctor can write the record
        Long assignedDoctorUserId = appt.getSchedule().getDoctor().getUser().getUserId();
        if (!Objects.equals(assignedDoctorUserId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not the doctor assigned to this appointment.");
        }

        if (appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(CONFLICT,
                    "Only CONFIRMED appointments can be completed with a record");
        }

        if (recordRepo.existsByAppointmentAppointmentId(appt.getAppointmentId())) {
            throw new ResponseStatusException(CONFLICT,
                    "A health record already exists for this appointment");
        }

        appt.setStatus(AppointmentStatus.COMPLETED);

        HealthRecord rec = new HealthRecord();
        rec.setAppointment(appt);
        rec.setDoctor(appt.getSchedule().getDoctor());
        rec.setPatient(appt.getPatient());
        rec.setDiagnosis(req.getDiagnosis());
        rec.setConsultationNotes(req.getConsultationNotes());

        return HealthRecordResponse.fromEntity(recordRepo.save(rec));
    }

    // FR-008: staff/doctors read patient records.
    public List<HealthRecordResponse> getByPatient(Long patientId) {
        return recordRepo.findByPatientPatientId(patientId)
                .stream()
                .map(HealthRecordResponse::fromEntity)
                .toList();
    }

    // Doctors can update diagnosis and consultation notes on an existing record
    @Transactional
    public HealthRecordResponse updateRecord(Long recordId, HealthRecordRequest req, Long currentUserId) {
        HealthRecord rec = recordRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health record not found with id: " + recordId));

        Long recordDoctorUserId = rec.getDoctor().getUser().getUserId();
        if (!Objects.equals(recordDoctorUserId, currentUserId)) {
            throw new ResponseStatusException(FORBIDDEN,
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
