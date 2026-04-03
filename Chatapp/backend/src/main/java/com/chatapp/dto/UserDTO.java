package com.chatapp.dto;

import com.chatapp.entity.User;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDTO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private User.UserStatus status;
    private LocalDateTime lastSeen;
}
