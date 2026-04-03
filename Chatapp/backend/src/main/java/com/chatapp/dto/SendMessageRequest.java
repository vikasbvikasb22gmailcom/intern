package com.chatapp.dto;

import com.chatapp.entity.Message;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SendMessageRequest {
    private Long receiverId;
    private Long groupId;
    private String content;
    private Message.MessageType type;
}
