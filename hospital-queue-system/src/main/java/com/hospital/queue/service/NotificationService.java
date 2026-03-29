package com.hospital.queue.service;

import com.hospital.queue.dto.NotificationDto;
import com.hospital.queue.entity.Notification;
import com.hospital.queue.entity.User;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.NotificationRepository;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final ModelMapper modelMapper;

    @Async
    public void sendEmailNotification(User user, String subject, String body) {
        Notification notification = Notification.builder()
                .user(user)
                .title(subject)
                .message(body)
                .type("EMAIL")
                .sent(false)
                .build();

        try {
            // Simulate email sending (log instead of actually sending in dev)
            log.info("=== EMAIL SIMULATION ===");
            log.info("To: {}", user.getEmail());
            log.info("Subject: {}", subject);
            log.info("Body: {}", body);
            log.info("========================");

            // Uncomment below to actually send emails when SMTP is configured:
            /*
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            */

            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    @Async
    public void sendSmsNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .title("SMS Notification")
                .message(message)
                .type("SMS")
                .sent(false)
                .build();

        try {
            // Simulate SMS sending
            log.info("=== SMS SIMULATION ===");
            log.info("To: {}", user.getPhone());
            log.info("Message: {}", message);
            log.info("======================");

            // Integrate Twilio or any SMS provider here

            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", user.getPhone(), e.getMessage());
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    public void sendAppointmentConfirmation(User patient, String doctorName,
                                            String date, String time, Integer queueNumber) {
        String subject = "Appointment Confirmed - Hospital Queue System";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been confirmed!\n\n" +
                "Doctor: Dr. %s\n" +
                "Date: %s\n" +
                "Time: %s\n" +
                "Queue Number: %d\n\n" +
                "Please arrive 10 minutes before your scheduled time.\n\n" +
                "Thank you,\nHospital Queue System",
                patient.getFullName(), doctorName, date, time, queueNumber
        );
        sendEmailNotification(patient, subject, body);
    }

    public void sendAppointmentReminder(User patient, String doctorName,
                                        String date, String time) {
        String subject = "Appointment Reminder - Hospital Queue System";
        String body = String.format(
                "Dear %s,\n\n" +
                "This is a reminder for your upcoming appointment.\n\n" +
                "Doctor: Dr. %s\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "Please be on time.\n\n" +
                "Thank you,\nHospital Queue System",
                patient.getFullName(), doctorName, date, time
        );
        sendEmailNotification(patient, subject, body);
    }

    public void sendCancellationNotification(User patient, String doctorName,
                                             String date, String time) {
        String subject = "Appointment Cancelled - Hospital Queue System";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been cancelled.\n\n" +
                "Doctor: Dr. %s\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "You can book a new appointment through our portal.\n\n" +
                "Thank you,\nHospital Queue System",
                patient.getFullName(), doctorName, date, time
        );
        sendEmailNotification(patient, subject, body);
    }

    public void sendQueueUpdateNotification(User patient, Integer currentPosition,
                                            Integer estimatedWait) {
        String message = String.format(
                "Queue Update: You are now #%d in line. Estimated wait: %d minutes.",
                currentPosition, estimatedWait
        );
        sendSmsNotification(patient, message);
    }

    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(n -> modelMapper.map(n, NotificationDto.class));
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", notificationId);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
