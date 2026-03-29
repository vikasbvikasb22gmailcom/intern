package com.hospital.queue.repository;

import com.hospital.queue.entity.Appointment;
import com.hospital.queue.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date, Pageable pageable);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNotOrderByQueueNumber(
            Long doctorId, LocalDate date, AppointmentStatus status);

    @Query("SELECT MAX(a.queueNumber) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date")
    Optional<Integer> findMaxQueueNumberForDoctorAndDate(Long doctorId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countActiveAppointmentsForDoctorAndDate(Long doctorId, LocalDate date);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId, LocalDate date, LocalTime time, AppointmentStatus status);

    List<Appointment> findByAppointmentDateAndReminderSentFalseAndStatusIn(
            LocalDate date, List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status = 'IN_PROGRESS'")
    Optional<Appointment> findCurrentAppointment(Long doctorId, LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status IN ('PENDING', 'CONFIRMED') ORDER BY a.queueNumber")
    List<Appointment> findQueueForDoctorAndDate(Long doctorId, LocalDate date);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
}
