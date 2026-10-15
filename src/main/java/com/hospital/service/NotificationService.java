package com.hospital.service;

import com.hospital.dto.NotificationDTO;
import com.hospital.entity.Notification;
import com.hospital.entity.User;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationDTO createNotification(User user, String title, String message, String type, Long appointmentId) {
        if (user == null) {
            log.warn("Cannot create notification for null user");
            return null;
        }
        try {
            Notification notification = new Notification(user, title, message, type, appointmentId);
            notification = notificationRepository.save(notification);
            log.info("Created notification [ID: {}, Type: {}] for user: {}", notification.getId(), type, user.getEmail());
            return mapToDTO(notification);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", user.getEmail(), e.getMessage());
            return null;
        }
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to notification");
        }

        notification.setRead(true);
        notification = notificationRepository.save(notification);
        return mapToDTO(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadForUser(userId);
    }

    private NotificationDTO mapToDTO(Notification n) {
        return new NotificationDTO(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.isRead(),
                n.getAppointmentId(),
                n.getCreatedAt()
        );
    }
}
