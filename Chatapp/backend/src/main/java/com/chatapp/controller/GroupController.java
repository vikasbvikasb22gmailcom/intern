package com.chatapp.controller;

import com.chatapp.dto.CreateGroupRequest;
import com.chatapp.dto.GroupDTO;
import com.chatapp.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired private GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(
                groupService.createGroup(userDetails.getUsername(), request));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupDTO>> getMyGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(groupService.getUserGroups(userDetails.getUsername()));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupDTO> addMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                groupService.addMember(groupId, userId, userDetails.getUsername()));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupDTO> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                groupService.removeMember(groupId, userId, userDetails.getUsername()));
    }
}
