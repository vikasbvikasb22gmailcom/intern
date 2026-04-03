package com.chatapp.controller;

import com.chatapp.dto.UserDTO;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    /** Get the currently authenticated user's profile. */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.toDTO(userService.getByUsername(userDetails.getUsername())));
    }

    /** Get any user's public profile by id. */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDTO(id));
    }

    /** Search users by username or display name. */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }

    /** Get all currently online users. */
    @GetMapping("/online")
    public ResponseEntity<List<UserDTO>> getOnlineUsers() {
        return ResponseEntity.ok(userService.getOnlineUsers());
    }

    /** Quick online-status check for a specific user. */
    @GetMapping("/{id}/status")
    public ResponseEntity<Boolean> isOnline(@PathVariable Long id) {
        return ResponseEntity.ok(userService.isUserOnline(id));
    }
}
