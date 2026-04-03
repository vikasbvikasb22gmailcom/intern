package com.chatapp.websocket;

import com.chatapp.dto.MessageDTO;
import com.chatapp.dto.SendMessageRequest;
import com.chatapp.dto.WebSocketMessage;
import com.chatapp.entity.User;
import com.chatapp.service.MessageService;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatWebSocketController {

    @Autowired private MessageService messageService;
    @Autowired private UserService userService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // ─── Connection lifecycle ────────────────────────────────────

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        if (event.getUser() != null) {
            String username = event.getUser().getName();
            String sessionId = event.getMessage().getHeaders()
                    .get("simpSessionId", String.class);
            userService.setUserOnline(username, sessionId != null ? sessionId : "");

            broadcastStatusChange(username, "ONLINE");
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            String username = event.getUser().getName();
            userService.setUserOffline(username);
            broadcastStatusChange(username, "OFFLINE");
        }
    }

    // ─── Private chat ────────────────────────────────────────────

    /**
     * Client sends to /app/chat.private
     * Payload: { receiverId, content, type }
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(SendMessageRequest request, Principal principal) {
        MessageDTO dto = messageService.sendPrivateMessage(principal.getName(), request);
        // Also echo back to sender so their UI updates instantly
        messagingTemplate.convertAndSendToUser(
                principal.getName(), "/queue/messages", dto);
    }

    // ─── Group chat ──────────────────────────────────────────────

    /**
     * Client sends to /app/chat.group
     * Payload: { groupId, content, type }
     */
    @MessageMapping("/chat.group")
    public void handleGroupMessage(SendMessageRequest request, Principal principal) {
        messageService.sendGroupMessage(principal.getName(), request);
        // Broadcast is done inside MessageService → /topic/group/{groupId}
    }

    // ─── Typing indicator ────────────────────────────────────────

    /**
     * Client sends to /app/chat.typing
     * Payload: { receiverId OR groupId, status: "TYPING" | "STOPPED" }
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(WebSocketMessage payload, Principal principal) {
        User sender = userService.getByUsername(principal.getName());

        WebSocketMessage indicator = WebSocketMessage.builder()
                .type("TYPING")
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .receiverId(payload.getReceiverId())
                .groupId(payload.getGroupId())
                .status(payload.getStatus())
                .timestamp(LocalDateTime.now())
                .build();

        if (payload.getGroupId() != null) {
            // Broadcast to group topic (everyone in the group sees it)
            messagingTemplate.convertAndSend(
                    "/topic/group/" + payload.getGroupId() + "/typing", indicator);
        } else if (payload.getReceiverId() != null) {
            // Send only to the other person
            String receiverUsername = userService.getById(payload.getReceiverId()).getUsername();
            messagingTemplate.convertAndSendToUser(
                    receiverUsername, "/queue/typing", indicator);
        }
    }

    // ─── Read receipt via WS ─────────────────────────────────────

    /**
     * Client sends to /app/chat.read
     * Payload: { senderId } — marks all messages from senderId as read
     */
    @MessageMapping("/chat.read")
    public void handleReadReceipt(WebSocketMessage payload, Principal principal) {
        User reader = userService.getByUsername(principal.getName());
        messageService.markMessagesAsRead(reader.getId(), payload.getSenderId());
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private void broadcastStatusChange(String username, String newStatus) {
        User user = userService.getByUsername(username);
        WebSocketMessage statusMsg = WebSocketMessage.builder()
                .type("STATUS_CHANGE")
                .senderId(user.getId())
                .senderUsername(username)
                .status(newStatus)
                .timestamp(LocalDateTime.now())
                .build();
        // Broadcast to all subscribers of /topic/status
        messagingTemplate.convertAndSend("/topic/status", statusMsg);
    }
}
