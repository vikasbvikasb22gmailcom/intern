package com.chatapp.service;

import com.chatapp.dto.MessageDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisCacheService {

    // In-memory storage (no Redis needed)
    private final Map<String, List<MessageDTO>> messageCache = new ConcurrentHashMap<>();
    private final Map<Long, String> userStatusMap = new ConcurrentHashMap<>();
    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();

    private static final int MESSAGE_CACHE_SIZE = 50;

    // ─── Message caching ─────────────────────────────────────────

    public void cacheMessage(String chatKey, MessageDTO message) {
        List<MessageDTO> list = messageCache.computeIfAbsent(chatKey, k -> new ArrayList<>());
        list.add(message);
        if (list.size() > MESSAGE_CACHE_SIZE) {
            list.remove(0);
        }
    }

    public List<MessageDTO> getCachedMessages(String chatKey) {
        return messageCache.getOrDefault(chatKey, new ArrayList<>());
    }

    public void clearMessageCache(String chatKey) {
        messageCache.remove(chatKey);
    }

    // ─── User status ─────────────────────────────────────────────

    public void setUserOnline(Long userId, String sessionId) {
        userStatusMap.put(userId, "ONLINE");
        userSessionMap.put(userId, sessionId);
    }

    public void setUserOffline(Long userId) {
        userStatusMap.remove(userId);
        userSessionMap.remove(userId);
    }

    public boolean isUserOnline(Long userId) {
        return "ONLINE".equals(userStatusMap.get(userId));
    }

    public String getUserStatus(Long userId) {
        return userStatusMap.getOrDefault(userId, "OFFLINE");
    }

    public void refreshUserSession(Long userId) {
        // No TTL needed for in-memory
    }

    // ─── Key helpers ─────────────────────────────────────────────

    public String buildPrivateChatKey(Long user1Id, Long user2Id) {
        long lo = Math.min(user1Id, user2Id);
        long hi = Math.max(user1Id, user2Id);
        return "private:" + lo + ":" + hi;
    }

    public String buildGroupChatKey(Long groupId) {
        return "group:" + groupId;
    }
}