package com.chatapp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WebSocketMessage {
    // type: CHAT, GROUP_CHAT, STATUS_CHANGE, TYPING, READ_RECEIPT
    private String type;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private Long groupId;
    private String content;
    private String status;   // ONLINE, OFFLINE, AWAY
    private Long messageId;
    private LocalDateTime timestamp;
}
