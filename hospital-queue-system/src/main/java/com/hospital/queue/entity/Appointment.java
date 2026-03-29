package com.hospital.queue.entity;

import com.hospital.queue.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(nullable = false)
    private Integer queueNumber;

    @Column
    private Integer estimatedWaitMinutes;

    @Column(length = 500)
    private String symptoms;

    @Column(length = 1000)
    private String doctorNotes;

    @Column(length = 1000)
    private String prescription;

    @Column
    private String cancellationReason;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    @Builder.Default
    private boolean reminderSent = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
