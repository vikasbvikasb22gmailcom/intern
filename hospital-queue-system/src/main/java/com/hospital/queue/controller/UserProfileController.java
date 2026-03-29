package com.hospital.queue.controller;

import com.hospital.queue.dto.ApiResponse;
import com.hospital.queue.dto.UserDto;
import com.hospital.queue.entity.User;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.UserRepository;
import com.hospital.queue.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profile", description = "Current user profile management")
public class UserProfileController {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @GetMapping
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(user, UserDto.class)));
    }

    @PutMapping
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getBloodGroup() != null) user.setBloodGroup(request.getBloodGroup());

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(user, UserDto.class), "Profile updated"));
    }

    @Data
    static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String address;
        private Integer age;
        private String bloodGroup;
    }
}
