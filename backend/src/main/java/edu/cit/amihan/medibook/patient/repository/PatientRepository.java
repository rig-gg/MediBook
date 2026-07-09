package edu.cit.amihan.medibook.patient.repository;

import edu.cit.amihan.medibook.patient.entity.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (p.contactNumber IS NOT NULL AND p.contactNumber LIKE CONCAT('%', :search, '%'))")
    List<Patient> searchPatients(@Param("search") String search);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Patient p ORDER BY p.fullName")
    List<Patient> findAllWithUser();
}