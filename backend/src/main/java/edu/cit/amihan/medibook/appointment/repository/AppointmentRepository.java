package edu.cit.amihan.medibook.appointment.repository;

import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Appointment> findByStatusOrderByCreatedAtAsc(AppointmentStatus status);

    List<Appointment> findAllByOrderByCreatedAtDesc();
}