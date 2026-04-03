package com.chatapp.dto;

import com.chatapp.entity.Message;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private Long receiverId;
    private Long groupId;
    private String content;
    private Message.MessageType type;
    private Message.MessageStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
