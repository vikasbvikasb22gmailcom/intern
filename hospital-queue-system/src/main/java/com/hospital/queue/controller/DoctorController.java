package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.DoctorDto;
import com.hospital.queue.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management and schedule endpoints")
public class DoctorController {

    private final DoctorService doctorService;

    // ── Public ──────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all available doctors (paginated)")
    public ResponseEntity<ApiResponse<Page<DoctorDto.DoctorResponse>>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String specialization) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<DoctorDto.DoctorResponse> result;

        if (search != null && !search.isBlank()) {
            result = doctorService.searchDoctors(search, pageable);
        } else if (specialization != null && !specialization.isBlank()) {
            result = doctorService.getDoctorsBySpecialization(specialization, pageable);
        } else {
            result = doctorService.getAllDoctors(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor details by ID")
    public ResponseEntity<ApiResponse<DoctorDto.DoctorResponse>> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorById(id)));
    }

    @GetMapping("/specializations")
    @Operation(summary = "Get list of all available specializations")
    public ResponseEntity<ApiResponse<List<String>>> getSpecializations() {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllSpecializations()));
    }

    @GetMapping("/{id}/schedule")
    @Operation(summary = "Get a doctor's weekly schedule")
    public ResponseEntity<ApiResponse<List<DoctorDto.ScheduleResponse>>> getDoctorSchedule(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorSchedule(id)));
    }

    // ── Admin Only ───────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new doctor (Admin only)")
    public ResponseEntity<ApiResponse<DoctorDto.DoctorResponse>> createDoctor(
            @Valid @RequestBody DoctorDto.CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorService.createDoctor(request), "Doctor created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update doctor profile (Admin or Doctor)")
    public ResponseEntity<ApiResponse<DoctorDto.DoctorResponse>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDto.UpdateDoctorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.updateDoctor(id, request), "Doctor updated"));
    }

    // ── Schedule Management ──────────────────────────────────────────────────

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add or update a day's schedule for a doctor")
    public ResponseEntity<ApiResponse<DoctorDto.ScheduleResponse>> addSchedule(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDto.ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorService.addOrUpdateSchedule(id, request), "Schedule saved"));
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a specific schedule entry")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        doctorService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule deleted"));
    }
}
