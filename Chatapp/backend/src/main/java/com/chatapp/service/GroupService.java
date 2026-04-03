package com.chatapp.service;

import com.chatapp.dto.CreateGroupRequest;
import com.chatapp.dto.GroupDTO;
import com.chatapp.entity.ChatGroup;
import com.chatapp.entity.Notification;
import com.chatapp.entity.User;
import com.chatapp.repository.ChatGroupRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired private ChatGroupRepository groupRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private NotificationService notificationService;

    @Transactional
    public GroupDTO createGroup(String creatorUsername, CreateGroupRequest req) {
        User creator = userService.getByUsername(creatorUsername);

        Set<User> members = new HashSet<>();
        members.add(creator);
        if (req.getMemberIds() != null) {
            req.getMemberIds().stream()
                    .map(userService::getById)
                    .forEach(members::add);
        }

        ChatGroup group = ChatGroup.builder()
                .name(req.getName())
                .description(req.getDescription())
                .createdBy(creator)
                .members(members)
                .build();

        group = groupRepository.save(group);
        final ChatGroup savedGroup = group;

        // Notify all members except creator
        members.stream()
                .filter(m -> !m.getId().equals(creator.getId()))
                .forEach(m -> notificationService.sendNotification(
                        m,
                        "Added to group: " + savedGroup.getName(),
                        creator.getDisplayName() + " added you to " + savedGroup.getName(),
                        Notification.NotificationType.GROUP_INVITE,
                        savedGroup.getId()));

        return toDTO(group);
    }

    public GroupDTO getGroup(Long groupId) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        return toDTO(group);
    }

    public List<GroupDTO> getUserGroups(String username) {
        User user = userService.getByUsername(username);
        return groupRepository.findGroupsByUserId(user.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public GroupDTO addMember(Long groupId, Long userId, String requesterUsername) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User requester = userService.getByUsername(requesterUsername);

        if (!groupRepository.isUserMemberOfGroup(groupId, requester.getId()))
            throw new RuntimeException("Only members can add others");

        User newMember = userService.getById(userId);
        group.getMembers().add(newMember);
        group = groupRepository.save(group);

        notificationService.sendNotification(
                newMember,
                "Added to group: " + group.getName(),
                requester.getDisplayName() + " added you to " + group.getName(),
                Notification.NotificationType.GROUP_INVITE,
                group.getId());

        return toDTO(group);
    }

    @Transactional
    public GroupDTO removeMember(Long groupId, Long userId, String requesterUsername) {
        ChatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User requester = userService.getByUsername(requesterUsername);

        boolean isCreator = group.getCreatedBy().getId().equals(requester.getId());
        boolean isSelf    = requester.getId().equals(userId);
        if (!isCreator && !isSelf)
            throw new RuntimeException("Only the group creator can remove members");

        User memberToRemove = userService.getById(userId);
        group.getMembers().remove(memberToRemove);
        group = groupRepository.save(group);
        return toDTO(group);
    }

    public GroupDTO toDTO(ChatGroup g) {
        return GroupDTO.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .createdById(g.getCreatedBy().getId())
                .createdByUsername(g.getCreatedBy().getUsername())
                .members(g.getMembers().stream()
                        .map(userService::toDTO)
                        .collect(Collectors.toSet()))
                .createdAt(g.getCreatedAt())
                .build();
    }
}
