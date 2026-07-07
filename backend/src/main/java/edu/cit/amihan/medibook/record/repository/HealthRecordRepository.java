package edu.cit.amihan.medibook.record.repository;

import edu.cit.amihan.medibook.record.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    boolean existsByAppointmentAppointmentId(Long appointmentId);

    Optional<HealthRecord> findByAppointmentAppointmentId(Long appointmentId);

    List<HealthRecord> findByPatientPatientId(Long patientId);
}