package com.chatapp.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupDTO {
    private Long id;
    private String name;
    private String description;
    private Long createdById;
    private String createdByUsername;
    private Set<UserDTO> members;
    private LocalDateTime createdAt;
}
