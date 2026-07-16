package edu.cit.amihan.medibook.schedule.repository;

import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorDoctorIdAndIsAvailableTrueOrderByStartTimeAsc(Long doctorId);

    List<DoctorSchedule> findByIsAvailableTrueOrderByStartTimeAsc();

    boolean existsByDoctorDoctorId(Long doctorId);

    List<DoctorSchedule> findByDoctorDoctorId(Long doctorId);

    // Database-level overlap check — prevents loading all rows into memory
    @Query("SELECT COUNT(s) > 0 FROM DoctorSchedule s " +
           "WHERE s.doctor.doctorId = :doctorId " +
           "AND (:excludeId IS NULL OR s.scheduleId <> :excludeId) " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlapping(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeScheduleId);

    // Pessimistic write lock: prevents two concurrent requests from both
    // reading isAvailable = true for the same slot before either commits.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DoctorSchedule s WHERE s.scheduleId = :scheduleId")
    Optional<DoctorSchedule> findByIdForUpdate(@Param("scheduleId") Long scheduleId);
}