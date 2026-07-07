package edu.cit.amihan.medibook.schedule.controller;

import edu.cit.amihan.medibook.schedule.dto.DoctorScheduleRequest;
import edu.cit.amihan.medibook.schedule.dto.DoctorScheduleResponse;
import edu.cit.amihan.medibook.schedule.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    // STAFF/ADMIN only — configuring doctor availability (FR-005)
    @PostMapping
    public ResponseEntity<DoctorScheduleResponse> createSchedule(
            @Valid @RequestBody DoctorScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.createSchedule(request));
    }

    // Any authenticated user (patients browsing, staff managing) — FR-003
    @GetMapping
    public ResponseEntity<List<DoctorScheduleResponse>> getAvailableSchedules(
            @RequestParam(required = false) Long doctorId) {
        return ResponseEntity.ok(scheduleService.getAvailableSchedules(doctorId));
    }
}