package com.chatapp.service;

import com.chatapp.dto.UserDTO;
import com.chatapp.entity.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RedisCacheService redisCache;

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public UserDTO getUserDTO(Long id) {
        return toDTO(getById(id));
    }

    public List<UserDTO> searchUsers(String query) {
        return userRepository.searchUsers(query)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<UserDTO> getOnlineUsers() {
        return userRepository.findOnlineUsers()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void setUserOnline(String username, String sessionId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus(User.UserStatus.ONLINE);
            userRepository.save(user);
            redisCache.setUserOnline(user.getId(), sessionId);
        });
    }

    @Transactional
    public void setUserOffline(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus(User.UserStatus.OFFLINE);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            redisCache.setUserOffline(user.getId());
        });
    }

    public boolean isUserOnline(Long userId) {
        return redisCache.isUserOnline(userId);
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .lastSeen(user.getLastSeen())
                .build();
    }
}
