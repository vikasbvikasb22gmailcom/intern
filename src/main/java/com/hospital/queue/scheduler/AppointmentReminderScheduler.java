package com.hospital.queue.scheduler;

import com.hospital.queue.entity.Appointment;
import com.hospital.queue.enums.AppointmentStatus;
import com.hospital.queue.repository.AppointmentRepository;
import com.hospital.queue.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    @Value("${app.queue.reminder-minutes-before:30}")
    private int reminderMinutesBefore;

    /**
     * Runs every 5 minutes — sends reminders for appointments
     * happening within the next `reminderMinutesBefore` minutes.
     */
    @Scheduled(fixedRateString = "300000") // every 5 minutes
    @Transactional
    public void sendUpcomingAppointmentReminders() {
        log.info("Running appointment reminder scheduler...");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime window = now.plusMinutes(reminderMinutesBefore);

        List<Appointment> upcomingAppointments = appointmentRepository
                .findByAppointmentDateAndReminderSentFalseAndStatusIn(
                        today,
                        List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING)
                );

        for (Appointment appointment : upcomingAppointments) {
            LocalTime apptTime = appointment.getAppointmentTime();
            // Send reminder if appointment is within the upcoming window
            if (!apptTime.isBefore(now) && !apptTime.isAfter(window)) {
                try {
                    notificationService.sendAppointmentReminder(
                            appointment.getPatient(),
                            appointment.getDoctor().getUser().getFullName(),
                            appointment.getAppointmentDate().toString(),
                            appointment.getAppointmentTime().toString()
                    );
                    appointment.setReminderSent(true);
                    appointmentRepository.save(appointment);
                    log.info("Reminder sent for appointment ID: {}", appointment.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for appointment {}: {}",
                            appointment.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Runs every day at midnight — marks missed appointments as NO_SHOW.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markNoShowAppointments() {
        log.info("Running no-show marker scheduler...");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Appointment> missed = appointmentRepository
                .findByAppointmentDateAndReminderSentFalseAndStatusIn(
                        yesterday,
                        List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING)
                );

        for (Appointment appointment : missed) {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            appointmentRepository.save(appointment);
            log.info("Marked appointment {} as NO_SHOW", appointment.getId());
        }
    }

    /**
     * Runs every hour — logs daily stats summary.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void logDailyStats() {
        LocalDate today = LocalDate.now();
        long active = appointmentRepository
                .countActiveAppointmentsForDoctorAndDate(0L, today);
        log.info("[Hourly Stats] Date: {} | Active appointments (approx): {}", today, active);
    }
}
