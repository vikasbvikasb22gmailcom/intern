package com.chatapp.controller;

import com.chatapp.dto.MessageDTO;
import com.chatapp.dto.SendMessageRequest;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired private MessageService messageService;
    @Autowired private UserService userService;

    // ─── Send ────────────────────────────────────────────────────

    @PostMapping("/private")
    public ResponseEntity<MessageDTO> sendPrivateMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(
                messageService.sendPrivateMessage(userDetails.getUsername(), request));
    }

    @PostMapping("/group")
    public ResponseEntity<MessageDTO> sendGroupMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(
                messageService.sendGroupMessage(userDetails.getUsername(), request));
    }

    // ─── History ─────────────────────────────────────────────────

    @GetMapping("/private/{otherUserId}")
    public ResponseEntity<List<MessageDTO>> getPrivateHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long otherUserId) {
        Long myId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(messageService.getPrivateChatHistory(myId, otherUserId));
    }

    @GetMapping("/private/{otherUserId}/paged")
    public ResponseEntity<List<MessageDTO>> getPrivateHistoryPaged(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long myId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(
                messageService.getPrivateChatHistoryPaged(myId, otherUserId, page, size));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MessageDTO>> getGroupHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(messageService.getGroupChatHistory(groupId));
    }

    @GetMapping("/group/{groupId}/paged")
    public ResponseEntity<List<MessageDTO>> getGroupHistoryPaged(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                messageService.getGroupChatHistoryPaged(groupId, page, size));
    }

    // ─── Read receipt ─────────────────────────────────────────────

    @PostMapping("/read/{senderId}")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long senderId) {
        Long myId = userService.getByUsername(userDetails.getUsername()).getId();
        messageService.markMessagesAsRead(myId, senderId);
        return ResponseEntity.ok().build();
    }
}
