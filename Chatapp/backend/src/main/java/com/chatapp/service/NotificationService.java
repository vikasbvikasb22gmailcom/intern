package com.chatapp.service;

import com.chatapp.dto.NotificationDTO;
import com.chatapp.entity.Notification;
import com.chatapp.entity.User;
import com.chatapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendNotification(User recipient, String title, String body,
                                  Notification.NotificationType type, Long referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .body(body)
                .type(type)
                .referenceId(referenceId)
                .build();

        notification = notificationRepository.save(notification);

        // Push via WebSocket to the user's private queue
        NotificationDTO dto = toDTO(notification);
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(), "/queue/notifications", dto);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .type(n.getType().name())
                .referenceId(n.getReferenceId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
