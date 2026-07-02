package edu.cit.amihan.medibook.patient.repository;

import edu.cit.amihan.medibook.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}