package edu.cit.amihan.medibook.schedule.service;

import edu.cit.amihan.medibook.common.exception.ResourceNotFoundException;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.schedule.dto.DoctorScheduleRequest;
import edu.cit.amihan.medibook.schedule.entity.DoctorSchedule;
import edu.cit.amihan.medibook.schedule.repository.DoctorScheduleRepository;
import edu.cit.amihan.medibook.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorScheduleService — Overlap Prevention & BR-003 Tests")
class DoctorScheduleServiceTest {

    @Mock private DoctorScheduleRepository scheduleRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks private DoctorScheduleService doctorScheduleService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .doctorId(10L)
                .user(User.builder().userId(1L).build())
                .fullName("Dr. Santos")
                .specialization("General Medicine")
                .build();
    }

    @Test
    @DisplayName("Create schedule — success for new slot")
    void createSchedule_success() {
        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(9));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(10));

        when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.existsOverlapping(eq(10L), any(), any(), isNull())).thenReturn(false);
        when(scheduleRepository.save(any())).thenAnswer(inv -> {
            DoctorSchedule s = inv.getArgument(0);
            s.setScheduleId(50L);
            return s;
        });

        var response = doctorScheduleService.createSchedule(request);

        assertEquals(50L, response.getScheduleId());
        assertTrue(response.getIsAvailable());
    }

    @Test
    @DisplayName("Create schedule — rejects overlapping slot (BR-003)")
    void createSchedule_overlappingSlot() {
        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(9));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(10));

        when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.existsOverlapping(eq(10L), any(), any(), isNull())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                doctorScheduleService.createSchedule(request));

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create schedule — throws if doctor not found")
    void createSchedule_doctorNotFound() {
        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(99L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(9));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(10));

        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                doctorScheduleService.createSchedule(request));
    }

    @Test
    @DisplayName("Create schedule — rejects endTime before startTime")
    void createSchedule_invalidTimeRange() {
        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(10));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(9));

        assertThrows(IllegalArgumentException.class, () ->
                doctorScheduleService.createSchedule(request));
    }

    @Test
    @DisplayName("Create schedule — rejects endTime equal to startTime")
    void createSchedule_equalTimes() {
        LocalDateTime time = LocalDateTime.now().plusDays(1).withHour(9);
        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(time);
        request.setEndTime(time);

        assertThrows(IllegalArgumentException.class, () ->
                doctorScheduleService.createSchedule(request));
    }

    @Test
    @DisplayName("Update schedule — rejects overlap with other slots")
    void updateSchedule_overlapping() {
        DoctorSchedule existing = DoctorSchedule.builder()
                .scheduleId(50L).doctor(doctor)
                .startTime(LocalDateTime.now().plusDays(1).withHour(9))
                .endTime(LocalDateTime.now().plusDays(1).withHour(10))
                .isAvailable(true).build();

        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(11));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(12));

        when(scheduleRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(scheduleRepository.existsOverlapping(eq(10L), any(), any(), eq(50L))).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                doctorScheduleService.updateSchedule(50L, request));
    }

    @Test
    @DisplayName("Update schedule — success when no overlap")
    void updateSchedule_success() {
        DoctorSchedule existing = DoctorSchedule.builder()
                .scheduleId(50L).doctor(doctor)
                .startTime(LocalDateTime.now().plusDays(1).withHour(9))
                .endTime(LocalDateTime.now().plusDays(1).withHour(10))
                .isAvailable(true).build();

        LocalDateTime newStart = LocalDateTime.now().plusDays(2).withHour(14);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(2).withHour(15);

        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(newStart);
        request.setEndTime(newEnd);

        when(scheduleRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(scheduleRepository.existsOverlapping(eq(10L), any(), any(), eq(50L))).thenReturn(false);
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = doctorScheduleService.updateSchedule(50L, request);

        assertEquals(newStart, response.getStartTime());
        assertEquals(newEnd, response.getEndTime());
    }

    @Test
    @DisplayName("Update schedule — throws if not found")
    void updateSchedule_notFound() {
        DoctorScheduleRequest request = new DoctorScheduleRequest();
        request.setDoctorId(10L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(9));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(10));

        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                doctorScheduleService.updateSchedule(999L, request));
    }
}
