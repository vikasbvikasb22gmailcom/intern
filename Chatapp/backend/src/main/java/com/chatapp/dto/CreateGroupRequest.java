package com.chatapp.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateGroupRequest {
    private String name;
    private String description;
    private List<Long> memberIds;
}
