package edu.cit.amihan.medibook.schedule.service;

import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.schedule.dto.DoctorScheduleRequest;
import edu.cit.amihan.medibook.schedule.dto.DoctorScheduleResponse;
import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import edu.cit.amihan.medibook.schedule.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public DoctorScheduleResponse createSchedule(DoctorScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + request.getDoctorId()));

        // BR-003: reject overlapping slots for the same doctor
        checkForOverlap(request.getStartTime(), request.getEndTime(), doctor.getDoctorId(), null);

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAvailable(true)
                .build();

        return DoctorScheduleResponse.fromEntity(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getAvailableSchedules(Long doctorId) {
        List<DoctorSchedule> schedules = (doctorId != null)
                ? scheduleRepository.findByDoctorDoctorIdAndIsAvailableTrueOrderByStartTimeAsc(doctorId)
                : scheduleRepository.findByIsAvailableTrueOrderByStartTimeAsc();

        return schedules.stream()
                .map(DoctorScheduleResponse::fromEntity)
                .toList();
    }

    @Transactional
    public DoctorScheduleResponse updateSchedule(Long scheduleId, DoctorScheduleRequest request) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + scheduleId));

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        // Check overlap excluding this schedule
        checkForOverlap(request.getStartTime(), request.getEndTime(),
                schedule.getDoctor().getDoctorId(), scheduleId);

        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return DoctorScheduleResponse.fromEntity(scheduleRepository.save(schedule));
    }

    private void checkForOverlap(
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime,
            Long doctorId,
            Long excludeScheduleId) {

        boolean hasOverlap = scheduleRepository.existsOverlapping(
                doctorId, startTime, endTime, excludeScheduleId);

        if (hasOverlap) {
            throw new IllegalArgumentException(
                    "This time slot overlaps with an existing schedule for this doctor.");
        }
    }
}