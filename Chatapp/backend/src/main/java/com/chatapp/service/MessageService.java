package com.chatapp.service;

import com.chatapp.dto.MessageDTO;
import com.chatapp.dto.SendMessageRequest;
import com.chatapp.entity.*;
import com.chatapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ChatGroupRepository groupRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private RedisCacheService redisCache;
    @Autowired private NotificationService notificationService;
    @Autowired private UserService userService;

    // ─── Send private message ────────────────────────────────────

    @Transactional
    public MessageDTO sendPrivateMessage(String senderUsername, SendMessageRequest req) {
        User sender = userService.getByUsername(senderUsername);
        User receiver = userService.getById(req.getReceiverId());

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(req.getContent())
                .type(req.getType() != null ? req.getType() : Message.MessageType.TEXT)
                .status(Message.MessageStatus.SENT)
                .build();

        message = messageRepository.save(message);
        MessageDTO dto = toDTO(message);

        // Cache in Redis
        String cacheKey = redisCache.buildPrivateChatKey(sender.getId(), receiver.getId());
        redisCache.cacheMessage(cacheKey, dto);

        // Deliver via WebSocket to receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(), "/queue/messages", dto);

        // Notify receiver if offline
        if (!redisCache.isUserOnline(receiver.getId())) {
            notificationService.sendNotification(
                    receiver,
                    "New message from " + sender.getDisplayName(),
                    req.getContent().length() > 60
                            ? req.getContent().substring(0, 60) + "…"
                            : req.getContent(),
                    Notification.NotificationType.NEW_MESSAGE,
                    message.getId());
        }

        return dto;
    }

    // ─── Send group message ──────────────────────────────────────

    @Transactional
    public MessageDTO sendGroupMessage(String senderUsername, SendMessageRequest req) {
        User sender = userService.getByUsername(senderUsername);

        // Must be final for use inside lambdas below
        final ChatGroup group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!groupRepository.isUserMemberOfGroup(group.getId(), sender.getId()))
            throw new RuntimeException("You are not a member of this group");

        Message message = Message.builder()
                .sender(sender)
                .group(group)
                .content(req.getContent())
                .type(req.getType() != null ? req.getType() : Message.MessageType.TEXT)
                .status(Message.MessageStatus.SENT)
                .build();

        final Message savedMessage = messageRepository.save(message);
        MessageDTO dto = toDTO(savedMessage);

        // Cache in memory
        String cacheKey = redisCache.buildGroupChatKey(group.getId());
        redisCache.cacheMessage(cacheKey, dto);

        // Broadcast to all group members via WebSocket topic
        messagingTemplate.convertAndSend("/topic/group/" + group.getId(), dto);

        // Build preview once as final so it's usable inside lambda
        final String preview = req.getContent().length() > 60
                ? req.getContent().substring(0, 60) + "…"
                : req.getContent();

        // Notify offline members
        group.getMembers().stream()
                .filter(m -> !m.getId().equals(sender.getId()))
                .filter(m -> !redisCache.isUserOnline(m.getId()))
                .forEach(m -> notificationService.sendNotification(
                        m,
                        group.getName() + ": " + sender.getDisplayName(),
                        preview,
                        Notification.NotificationType.NEW_MESSAGE,
                        savedMessage.getId()));

        return dto;
    }

    // ─── History ─────────────────────────────────────────────────

    public List<MessageDTO> getPrivateChatHistory(Long user1Id, Long user2Id) {
        String cacheKey = redisCache.buildPrivateChatKey(user1Id, user2Id);
        List<MessageDTO> cached = redisCache.getCachedMessages(cacheKey);
        if (!cached.isEmpty()) return cached;

        return messageRepository.findPrivateChatHistory(user1Id, user2Id)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MessageDTO> getPrivateChatHistoryPaged(Long user1Id, Long user2Id, int page, int size) {
        return messageRepository.findPrivateChatHistoryPaged(
                user1Id, user2Id,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MessageDTO> getGroupChatHistory(Long groupId) {
        String cacheKey = redisCache.buildGroupChatKey(groupId);
        List<MessageDTO> cached = redisCache.getCachedMessages(cacheKey);
        if (!cached.isEmpty()) return cached;

        return messageRepository.findGroupChatHistory(groupId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MessageDTO> getGroupChatHistoryPaged(Long groupId, int page, int size) {
        return messageRepository.findGroupChatHistoryPaged(
                groupId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ─── Read receipts ───────────────────────────────────────────

    @Transactional
    public void markMessagesAsRead(Long receiverId, Long senderId) {
        messageRepository.markMessagesAsRead(receiverId, senderId);

        // Inform sender that their messages were read
        User receiver = userService.getById(receiverId);
        messagingTemplate.convertAndSendToUser(
                userService.getById(senderId).getUsername(),
                "/queue/read-receipt",
                java.util.Map.of(
                        "type", "READ_RECEIPT",
                        "readBy", receiverId,
                        "readAt", LocalDateTime.now().toString()));
    }

    // ─── Mapper ──────────────────────────────────────────────────

    public MessageDTO toDTO(Message m) {
        return MessageDTO.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderUsername(m.getSender().getUsername())
                .senderDisplayName(m.getSender().getDisplayName())
                .receiverId(m.getReceiver() != null ? m.getReceiver().getId() : null)
                .groupId(m.getGroup() != null ? m.getGroup().getId() : null)
                .content(m.getContent())
                .type(m.getType())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .readAt(m.getReadAt())
                .build();
    }
}
