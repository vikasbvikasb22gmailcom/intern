package com.hospital.queue.dto;

import com.hospital.queue.enums.DayOfWeek;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

public class DoctorDto {

    @Data
    public static class CreateDoctorRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        private String email;

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid 10-digit phone number required")
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 6)
        private String password;

        @NotBlank(message = "Specialization is required")
        private String specialization;

        private String qualification;
        private String licenseNumber;
        private Integer consultationDurationMinutes = 20;
        private Double consultationFee = 500.0;
        private Integer experienceYears;
        private String bio;
        private Integer maxPatientsPerDay = 20;
    }

    @Data
    public static class UpdateDoctorRequest {
        private String specialization;
        private String qualification;
        private String licenseNumber;
        private Integer consultationDurationMinutes;
        private Double consultationFee;
        private Integer experienceYears;
        private String bio;
        private Boolean available;
        private Integer maxPatientsPerDay;
    }

    @Data
    public static class DoctorResponse {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String specialization;
        private String qualification;
        private String licenseNumber;
        private Integer consultationDurationMinutes;
        private Double consultationFee;
        private Integer experienceYears;
        private String bio;
        private boolean available;
        private Integer maxPatientsPerDay;
        private List<ScheduleResponse> schedules;
    }

    @Data
    public static class ScheduleRequest {
        @NotNull(message = "Day of week is required")
        private DayOfWeek dayOfWeek;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        private LocalTime endTime;

        private Boolean isActive = true;
        private String notes;
    }

    @Data
    public static class ScheduleResponse {
        private Long id;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean isActive;
        private String notes;
    }
}
