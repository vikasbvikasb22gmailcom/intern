package com.chatapp.repository;

import com.chatapp.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Private chat history between two users
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findPrivateChatHistory(Long user1Id, Long user2Id);

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findPrivateChatHistoryPaged(Long user1Id, Long user2Id, Pageable pageable);

    // Group chat history
    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.createdAt ASC")
    List<Message> findGroupChatHistory(Long groupId);

    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId ORDER BY m.createdAt DESC")
    Page<Message> findGroupChatHistoryPaged(Long groupId, Pageable pageable);

    // Unread messages
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.status != 'READ'")
    List<Message> findUnreadMessages(Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.sender.id = :senderId AND m.status != 'READ'")
    long countUnreadMessagesFromSender(Long userId, Long senderId);

    // Mark messages as read
    @Modifying
    @Query("UPDATE Message m SET m.status = 'READ' WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.status != 'READ'")
    void markMessagesAsRead(Long receiverId, Long senderId);
}
