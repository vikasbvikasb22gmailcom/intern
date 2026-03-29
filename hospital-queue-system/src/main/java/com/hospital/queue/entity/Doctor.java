package com.hospital.queue.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String specialization;

    @Column
    private String qualification;

    @Column
    private String licenseNumber;

    @Column(nullable = false)
    @Builder.Default
    private Integer consultationDurationMinutes = 20;

    @Column(nullable = false)
    @Builder.Default
    private Double consultationFee = 500.0;

    @Column
    private Integer experienceYears;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxPatientsPerDay = 20;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DoctorSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
