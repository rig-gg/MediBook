package edu.cit.amihan.medibook.schedule.repository;

import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorDoctorIdAndIsAvailableTrueOrderByStartTimeAsc(Long doctorId);

    List<DoctorSchedule> findByIsAvailableTrueOrderByStartTimeAsc();

    // Pessimistic write lock: prevents two concurrent requests from both
    // reading isAvailable = true for the same slot before either commits.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DoctorSchedule s WHERE s.scheduleId = :scheduleId")
    Optional<DoctorSchedule> findByIdForUpdate(@Param("scheduleId") Long scheduleId);
}