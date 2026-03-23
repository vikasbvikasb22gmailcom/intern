package com.hospital.queue.dto;

import com.hospital.queue.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private String address;
    private Integer age;
    private String bloodGroup;
    private boolean enabled;
    private LocalDateTime createdAt;
}
