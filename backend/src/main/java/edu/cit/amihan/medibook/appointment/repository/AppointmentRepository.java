package edu.cit.amihan.medibook.appointment.repository;

import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = {"patient", "schedule.doctor"})
    List<Appointment> findByPatientPatientIdOrderByCreatedAtDesc(Long patientId);

    @EntityGraph(attributePaths = {"patient", "schedule.doctor"})
    List<Appointment> findByStatusOrderByCreatedAtAsc(AppointmentStatus status);

    @EntityGraph(attributePaths = {"patient", "schedule.doctor"})
    List<Appointment> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"patient", "schedule.doctor"})
    List<Appointment> findByScheduleDoctorDoctorIdOrderByCreatedAtDesc(Long doctorId);

    boolean existsByScheduleDoctorDoctorIdAndPatientPatientId(Long doctorId, Long patientId);
}