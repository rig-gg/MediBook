package edu.cit.amihan.medibook.clinicstaff.repository;

import edu.cit.amihan.medibook.clinicstaff.entity.ClinicStaff;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClinicStaffRepository extends JpaRepository<ClinicStaff, Long> {

    Optional<ClinicStaff> findByUserUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM ClinicStaff s WHERE " +
           "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (s.contactNumber IS NOT NULL AND s.contactNumber LIKE CONCAT('%', :search, '%')) " +
           "OR (s.position IS NOT NULL AND LOWER(s.position) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ClinicStaff> searchStaff(@Param("search") String search);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM ClinicStaff s ORDER BY s.fullName")
    List<ClinicStaff> findAllWithUser();
}
