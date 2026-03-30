package com.hospital.queue.dto;

import com.hospital.queue.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDto {

    @Data
    public static class BookAppointmentRequest {
        @NotNull(message = "Doctor ID is required")
        private Long doctorId;

        @NotNull(message = "Appointment date is required")
        private LocalDate appointmentDate;

        @NotNull(message = "Appointment time is required")
        private LocalTime appointmentTime;

        private String symptoms;
    }

    @Data
    public static class UpdateAppointmentRequest {
        private AppointmentStatus status;
        private String doctorNotes;
        private String prescription;
        private String cancellationReason;
    }

    @Data
    public static class AppointmentResponse {
        private Long id;
        private Long patientId;
        private String patientName;
        private String patientEmail;
        private String patientPhone;
        private Long doctorId;
        private String doctorName;
        private String doctorSpecialization;
        private LocalDate appointmentDate;
        private LocalTime appointmentTime;
        private AppointmentStatus status;
        private Integer queueNumber;
        private Integer estimatedWaitMinutes;
        private String symptoms;
        private String doctorNotes;
        private String prescription;
        private String cancellationReason;
        private LocalDateTime createdAt;
    }

    @Data
    public static class QueueStatusResponse {
        private Long appointmentId;
        private Integer queueNumber;
        private Integer currentQueueNumber;
        private Integer patientsAhead;
        private Integer estimatedWaitMinutes;
        private AppointmentStatus status;
        private String doctorName;
        private LocalTime appointmentTime;
        private String message;
    }

    @Data
    public static class RescheduleRequest {
        @NotNull(message = "New date is required")
        private LocalDate newDate;

        @NotNull(message = "New time is required")
        private LocalTime newTime;
    }
}
