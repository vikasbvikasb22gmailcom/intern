package com.hospital.queue.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointmentsToday;
    private long pendingAppointmentsToday;
    private long completedAppointmentsToday;
    private long cancelledAppointmentsToday;
    private long totalAppointmentsAllTime;
    private Map<String, Long> appointmentsBySpecialization;
    private Map<String, Long> appointmentsByStatus;
}
