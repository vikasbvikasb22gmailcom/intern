package com.chatapp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String body;
    private String type;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
}
