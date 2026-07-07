package edu.cit.amihan.medibook.patient.repository;

import edu.cit.amihan.medibook.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    // add to PatientRepository.java
    Optional<Patient> findByUserUserId(Long userId);
}