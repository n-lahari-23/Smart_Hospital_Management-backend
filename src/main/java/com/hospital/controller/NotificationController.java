package com.hospital.controller;

import com.hospital.dto.NotificationDTO;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import com.hospital.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private Long getAuthUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication authentication) {
        Long userId = getAuthUserId(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = getAuthUserId(authentication);
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(Authentication authentication, @PathVariable Long id) {
        Long userId = getAuthUserId(authentication);
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Long userId = getAuthUserId(authentication);
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", true, "updatedCount", updated));
    }
}
