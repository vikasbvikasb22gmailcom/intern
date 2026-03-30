package com.hospital.queue.service;

import com.hospital.queue.dto.AppointmentDto;
import com.hospital.queue.entity.Appointment;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.User;
import com.hospital.queue.enums.AppointmentStatus;
import com.hospital.queue.enums.DayOfWeek;
import com.hospital.queue.exception.BadRequestException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.exception.UnauthorizedException;
import com.hospital.queue.repository.AppointmentRepository;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.DoctorScheduleRepository;
import com.hospital.queue.repository.UserRepository;
import com.hospital.queue.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // ─── Book Appointment ────────────────────────────────────────────────────

    @Transactional
    public AppointmentDto.AppointmentResponse bookAppointment(AppointmentDto.BookAppointmentRequest request) {
        User patient = getCurrentUser();
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.getDoctorId()));

        validateAppointmentSlot(doctor, request.getAppointmentDate(), request.getAppointmentTime());

        long dailyCount = appointmentRepository
                .countActiveAppointmentsForDoctorAndDate(doctor.getId(), request.getAppointmentDate());
        if (dailyCount >= doctor.getMaxPatientsPerDay()) {
            throw new BadRequestException("Doctor's schedule is fully booked for " + request.getAppointmentDate());
        }

        int queueNumber = appointmentRepository
                .findMaxQueueNumberForDoctorAndDate(doctor.getId(), request.getAppointmentDate())
                .orElse(0) + 1;

        int estimatedWait = calculateEstimatedWait(doctor, request.getAppointmentDate(), queueNumber);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.CONFIRMED)
                .queueNumber(queueNumber)
                .estimatedWaitMinutes(estimatedWait)
                .symptoms(request.getSymptoms())
                .confirmedAt(LocalDateTime.now())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Send confirmation notification
        notificationService.sendAppointmentConfirmation(
                patient,
                doctor.getUser().getFullName(),
                request.getAppointmentDate().toString(),
                request.getAppointmentTime().toString(),
                queueNumber
        );

        // Broadcast queue update to all subscribers of this doctor
        broadcastQueueUpdate(doctor.getId(), request.getAppointmentDate());

        return mapToResponse(saved);
    }

    // ─── Get Appointments ────────────────────────────────────────────────────

    public Page<AppointmentDto.AppointmentResponse> getMyAppointments(Pageable pageable) {
        User patient = getCurrentUser();
        return appointmentRepository.findByPatientId(patient.getId(), pageable)
                .map(this::mapToResponse);
    }

    public Page<AppointmentDto.AppointmentResponse> getDoctorAppointments(Long doctorId,
                                                                           LocalDate date,
                                                                           Pageable pageable) {
        if (date != null) {
            return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date, pageable)
                    .map(this::mapToResponse);
        }
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::mapToResponse);
    }

    public AppointmentDto.AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        assertAccessToAppointment(appointment);
        return mapToResponse(appointment);
    }

    // ─── Queue Tracking ──────────────────────────────────────────────────────

    public AppointmentDto.QueueStatusResponse getQueueStatus(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        assertAccessToAppointment(appointment);

        return buildQueueStatus(appointment);
    }

    public List<AppointmentDto.QueueStatusResponse> getLiveDoctorQueue(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        List<Appointment> queue = appointmentRepository.findQueueForDoctorAndDate(
                doctorId, LocalDate.now());

        return queue.stream().map(this::buildQueueStatus).collect(Collectors.toList());
    }

    // ─── Cancel Appointment ──────────────────────────────────────────────────

    @Transactional
    public AppointmentDto.AppointmentResponse cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        assertAccessToAppointment(appointment);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setCancelledAt(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);

        notificationService.sendCancellationNotification(
                appointment.getPatient(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString()
        );

        broadcastQueueUpdate(appointment.getDoctor().getId(), appointment.getAppointmentDate());

        return mapToResponse(saved);
    }

    // ─── Reschedule ──────────────────────────────────────────────────────────

    @Transactional
    public AppointmentDto.AppointmentResponse rescheduleAppointment(Long id,
                                                                     AppointmentDto.RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        assertAccessToAppointment(appointment);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot reschedule a " + appointment.getStatus() + " appointment");
        }

        Doctor doctor = appointment.getDoctor();
        validateAppointmentSlot(doctor, request.getNewDate(), request.getNewTime());

        // Remove from old queue and add to new
        int newQueueNumber = appointmentRepository
                .findMaxQueueNumberForDoctorAndDate(doctor.getId(), request.getNewDate())
                .orElse(0) + 1;

        appointment.setAppointmentDate(request.getNewDate());
        appointment.setAppointmentTime(request.getNewTime());
        appointment.setQueueNumber(newQueueNumber);
        appointment.setEstimatedWaitMinutes(calculateEstimatedWait(doctor, request.getNewDate(), newQueueNumber));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Appointment saved = appointmentRepository.save(appointment);

        broadcastQueueUpdate(doctor.getId(), request.getNewDate());

        return mapToResponse(saved);
    }

    // ─── Doctor: Update Status ───────────────────────────────────────────────

    @Transactional
    public AppointmentDto.AppointmentResponse updateAppointmentStatus(Long id,
                                                                       AppointmentDto.UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        if (request.getStatus() != null) appointment.setStatus(request.getStatus());
        if (request.getDoctorNotes() != null) appointment.setDoctorNotes(request.getDoctorNotes());
        if (request.getPrescription() != null) appointment.setPrescription(request.getPrescription());
        if (request.getCancellationReason() != null) appointment.setCancellationReason(request.getCancellationReason());

        if (request.getStatus() == AppointmentStatus.COMPLETED) {
            appointment.setCompletedAt(LocalDateTime.now());
        }

        Appointment saved = appointmentRepository.save(appointment);

        broadcastQueueUpdate(appointment.getDoctor().getId(), appointment.getAppointmentDate());

        return mapToResponse(saved);
    }

    // ─── Admin: All Appointments ─────────────────────────────────────────────

    public Page<AppointmentDto.AppointmentResponse> getAllAppointments(AppointmentStatus status,
                                                                        Pageable pageable) {
        if (status != null) {
            return appointmentRepository.findByStatus(status, pageable).map(this::mapToResponse);
        }
        return appointmentRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ─── Available Slots ─────────────────────────────────────────────────────

    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        DayOfWeek day = DayOfWeek.valueOf(date.getDayOfWeek().name());
        var schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, day)
                .orElseThrow(() -> new BadRequestException("Doctor is not available on " + day));

        if (!schedule.isActive()) {
            throw new BadRequestException("Doctor is not available on " + day);
        }

        List<Appointment> existing = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusNotOrderByQueueNumber(
                        doctorId, date, AppointmentStatus.CANCELLED);

        List<LocalTime> bookedTimes = existing.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toList());

        List<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime current = schedule.getStartTime();
        int duration = doctor.getConsultationDurationMinutes();

        while (current.plusMinutes(duration).compareTo(schedule.getEndTime()) <= 0) {
            if (!bookedTimes.contains(current)) {
                slots.add(current);
            }
            current = current.plusMinutes(duration);
        }

        return slots;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validateAppointmentSlot(Doctor doctor, LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book an appointment in the past");
        }

        DayOfWeek day = DayOfWeek.valueOf(date.getDayOfWeek().name());
        var scheduleOpt = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), day);
        if (scheduleOpt.isEmpty() || !scheduleOpt.get().isActive()) {
            throw new BadRequestException("Doctor is not available on " + day);
        }

        var schedule = scheduleOpt.get();
        if (time.isBefore(schedule.getStartTime()) || time.isAfter(schedule.getEndTime())) {
            throw new BadRequestException("Selected time is outside doctor's working hours");
        }

        boolean slotTaken = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        doctor.getId(), date, time, AppointmentStatus.CANCELLED);
        if (slotTaken) {
            throw new BadRequestException("This time slot is already booked");
        }
    }

    private int calculateEstimatedWait(Doctor doctor, LocalDate date, int queueNumber) {
        long completed = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusNotOrderByQueueNumber(
                        doctor.getId(), date, AppointmentStatus.CANCELLED)
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        int ahead = (int) (queueNumber - 1 - completed);
        return Math.max(0, ahead * doctor.getConsultationDurationMinutes());
    }

    private AppointmentDto.QueueStatusResponse buildQueueStatus(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        LocalDate date = appointment.getAppointmentDate();

        List<Appointment> queue = appointmentRepository.findQueueForDoctorAndDate(doctor.getId(), date);
        int position = (int) queue.stream()
                .filter(a -> a.getQueueNumber() < appointment.getQueueNumber())
                .count() + 1;

        int estimatedWait = Math.max(0, (position - 1) * doctor.getConsultationDurationMinutes());

        String message = switch (appointment.getStatus()) {
            case CONFIRMED -> "Your appointment is confirmed. You are #" + position + " in queue.";
            case IN_PROGRESS -> "You are currently being attended to.";
            case COMPLETED -> "Your appointment is completed.";
            case CANCELLED -> "Your appointment has been cancelled.";
            default -> "Waiting for confirmation.";
        };

        AppointmentDto.QueueStatusResponse status = new AppointmentDto.QueueStatusResponse();
        status.setAppointmentId(appointment.getId());
        status.setQueueNumber(appointment.getQueueNumber());
        status.setCurrentQueueNumber(position);
        status.setPatientsAhead(Math.max(0, position - 1));
        status.setEstimatedWaitMinutes(estimatedWait);
        status.setStatus(appointment.getStatus());
        status.setDoctorName(doctor.getUser().getFullName());
        status.setAppointmentTime(appointment.getAppointmentTime());
        status.setMessage(message);
        return status;
    }

    private void broadcastQueueUpdate(Long doctorId, LocalDate date) {
        try {
            List<Appointment> queue = appointmentRepository.findQueueForDoctorAndDate(doctorId, date);
            List<AppointmentDto.QueueStatusResponse> queueStatus = queue.stream()
                    .map(this::buildQueueStatus)
                    .collect(Collectors.toList());
            messagingTemplate.convertAndSend("/topic/queue/" + doctorId, queueStatus);
            log.debug("Queue update broadcast for doctor {} on {}", doctorId, date);
        } catch (Exception e) {
            log.error("Failed to broadcast queue update: {}", e.getMessage());
        }
    }

    private void assertAccessToAppointment(Appointment appointment) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isDoctor = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        boolean isOwner = appointment.getPatient().getId().equals(currentUser.getId());
        boolean isDoctorOfAppointment = appointment.getDoctor().getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner && !(isDoctor && isDoctorOfAppointment)) {
            throw new UnauthorizedException("You do not have access to this appointment");
        }
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", userDetails.getId()));
    }

    private AppointmentDto.AppointmentResponse mapToResponse(Appointment a) {
        AppointmentDto.AppointmentResponse r = new AppointmentDto.AppointmentResponse();
        r.setId(a.getId());
        r.setPatientId(a.getPatient().getId());
        r.setPatientName(a.getPatient().getFullName());
        r.setPatientEmail(a.getPatient().getEmail());
        r.setPatientPhone(a.getPatient().getPhone());
        r.setDoctorId(a.getDoctor().getId());
        r.setDoctorName(a.getDoctor().getUser().getFullName());
        r.setDoctorSpecialization(a.getDoctor().getSpecialization());
        r.setAppointmentDate(a.getAppointmentDate());
        r.setAppointmentTime(a.getAppointmentTime());
        r.setStatus(a.getStatus());
        r.setQueueNumber(a.getQueueNumber());
        r.setEstimatedWaitMinutes(a.getEstimatedWaitMinutes());
        r.setSymptoms(a.getSymptoms());
        r.setDoctorNotes(a.getDoctorNotes());
        r.setPrescription(a.getPrescription());
        r.setCancellationReason(a.getCancellationReason());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
