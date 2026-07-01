package edu.cit.amihan.medibook.doctor.repository;

import edu.cit.amihan.medibook.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser_UserId(Long userId);
}