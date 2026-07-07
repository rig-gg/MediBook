package edu.cit.amihan.medibook.record.dto;

import edu.cit.amihan.medibook.record.entity.HealthRecord;
import java.time.LocalDateTime;

public class HealthRecordResponse {

    private Long recordId;
    private Long appointmentId;
    private String doctorName;
    private String patientName;
    private String diagnosis;
    private String consultationNotes;
    private LocalDateTime recordedAt;

    public static HealthRecordResponse fromEntity(HealthRecord rec) {
        HealthRecordResponse r = new HealthRecordResponse();
        r.recordId = rec.getRecordId();
        r.appointmentId = rec.getAppointment().getAppointmentId();
        r.doctorName = rec.getDoctor().getFullName();
        r.patientName = rec.getPatient().getFullName();
        r.diagnosis = rec.getDiagnosis();
        r.consultationNotes = rec.getConsultationNotes();
        r.recordedAt = rec.getRecordedAt();
        return r;
    }

    public Long getRecordId() { return recordId; }
    public Long getAppointmentId() { return appointmentId; }
    public String getDoctorName() { return doctorName; }
    public String getPatientName() { return patientName; }
    public String getDiagnosis() { return diagnosis; }
    public String getConsultationNotes() { return consultationNotes; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}