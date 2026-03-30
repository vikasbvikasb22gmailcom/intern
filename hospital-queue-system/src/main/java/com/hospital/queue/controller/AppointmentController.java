package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.AppointmentDto;
import com.hospital.queue.enums.AppointmentStatus;
import com.hospital.queue.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Appointments", description = "Booking, cancellation, and queue tracking")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ── Patient: Book ────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book a new appointment")
    public ResponseEntity<ApiResponse<AppointmentDto.AppointmentResponse>> bookAppointment(
            @Valid @RequestBody AppointmentDto.BookAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(appointmentService.bookAppointment(request), "Appointment booked"));
    }

    // ── Patient: My Appointments ─────────────────────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get logged-in patient's appointments (paginated)")
    public ResponseEntity<ApiResponse<Page<AppointmentDto.AppointmentResponse>>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending());
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getMyAppointments(pageable)));
    }

    // ── Get Available Slots ───────────────────────────────────────────────────

    @GetMapping("/available-slots")
    @Operation(summary = "Get available time slots for a doctor on a given date")
    public ResponseEntity<ApiResponse<List<LocalTime>>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAvailableSlots(doctorId, date)));
    }

    // ── Appointment Details ───────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment details by ID")
    public ResponseEntity<ApiResponse<AppointmentDto.AppointmentResponse>> getAppointment(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentById(id)));
    }

    // ── Queue Status ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/queue-status")
    @Operation(summary = "Get live queue status for a specific appointment")
    public ResponseEntity<ApiResponse<AppointmentDto.QueueStatusResponse>> getQueueStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getQueueStatus(id)));
    }

    @GetMapping("/queue/doctor/{doctorId}")
    @Operation(summary = "Get today's full live queue for a doctor")
    public ResponseEntity<ApiResponse<List<AppointmentDto.QueueStatusResponse>>> getDoctorLiveQueue(
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getLiveDoctorQueue(doctorId)));
    }

    // ── Cancel & Reschedule ───────────────────────────────────────────────────

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<ApiResponse<AppointmentDto.AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.cancelAppointment(id, reason), "Appointment cancelled"));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Reschedule an existing appointment")
    public ResponseEntity<ApiResponse<AppointmentDto.AppointmentResponse>> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDto.RescheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.rescheduleAppointment(id, request), "Appointment rescheduled"));
    }

    // ── Doctor: Update Status ─────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Update appointment status, add notes/prescription (Doctor/Admin)")
    public ResponseEntity<ApiResponse<AppointmentDto.AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDto.UpdateAppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.updateAppointmentStatus(id, request), "Appointment updated"));
    }

    // ── Doctor: View Own Schedule ─────────────────────────────────────────────

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Get all appointments for a doctor (optionally filtered by date)")
    public ResponseEntity<ApiResponse<Page<AppointmentDto.AppointmentResponse>>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("queueNumber").ascending());
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getDoctorAppointments(doctorId, date, pageable)));
    }

    // ── Admin: All Appointments ───────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all appointments with optional status filter (Admin only)")
    public ResponseEntity<ApiResponse<Page<AppointmentDto.AppointmentResponse>>> getAllAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending());
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getAllAppointments(status, pageable)));
    }
}
