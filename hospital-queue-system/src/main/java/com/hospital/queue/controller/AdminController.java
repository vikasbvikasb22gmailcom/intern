package com.hospital.queue.controller;

import com.hospital.queue.dto.AdminDashboardDto;
import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.UserDto;
import com.hospital.queue.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin dashboard and user management")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get system-wide dashboard statistics")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    @GetMapping("/patients")
    @Operation(summary = "List all patients with optional search (paginated)")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllPatients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllPatients(search, pageable)));
    }

    @GetMapping("/patients/{id}")
    @Operation(summary = "Get a specific patient by ID")
    public ResponseEntity<ApiResponse<UserDto>> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPatientById(id)));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.toggleUserStatus(id), "User status updated"));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Permanently delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
