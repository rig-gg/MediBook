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

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    public DoctorScheduleResponse createSchedule(DoctorScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + request.getDoctorId()));

        // BR-003: reject overlapping slots for the same doctor
        boolean hasOverlap = scheduleRepository
                .findByDoctorDoctorIdAndIsAvailableTrueOrderByStartTimeAsc(doctor.getDoctorId())
                .stream()
                .anyMatch(existing ->
                        request.getStartTime().isBefore(existing.getEndTime()) &&
                                request.getEndTime().isAfter(existing.getStartTime())
                );

        if (hasOverlap) {
            throw new IllegalArgumentException(
                    "This time slot overlaps with an existing schedule for this doctor.");
        }

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAvailable(true)
                .build();

        return DoctorScheduleResponse.fromEntity(scheduleRepository.save(schedule));
    }

    public List<DoctorScheduleResponse> getAvailableSchedules(Long doctorId) {
        List<DoctorSchedule> schedules = (doctorId != null)
                ? scheduleRepository.findByDoctorDoctorIdAndIsAvailableTrueOrderByStartTimeAsc(doctorId)
                : scheduleRepository.findByIsAvailableTrueOrderByStartTimeAsc();

        return schedules.stream()
                .map(DoctorScheduleResponse::fromEntity)
                .toList();
    }
}