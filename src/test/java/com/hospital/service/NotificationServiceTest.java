package com.hospital.service;

import com.hospital.dto.NotificationDTO;
import com.hospital.entity.Notification;
import com.hospital.entity.User;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "john@gmail.com", "pass", "PATIENT", "9999999999");
        user.setId(5L);
    }

    @Test
    void testCreateNotification_Success() {
        Notification saved = new Notification(user, "Title", "Message", "BOOKED", 1L);
        saved.setId(10L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationDTO dto = notificationService.createNotification(user, "Title", "Message", "BOOKED", 1L);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("BOOKED", dto.getType());
        assertFalse(dto.isRead());
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndReadFalse(5L)).thenReturn(3L);
        assertEquals(3L, notificationService.getUnreadCount(5L));
    }

    @Test
    void testMarkAsRead_Success() {
        Notification n = new Notification(user, "Title", "Message", "BOOKED", 1L);
        n.setId(10L);
        n.setRead(false);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationDTO result = notificationService.markAsRead(10L, 5L);
        assertTrue(result.isRead());
    }

    @Test
    void testMarkAsRead_Unauthorized() {
        Notification n = new Notification(user, "Title", "Message", "BOOKED", 1L);
        n.setId(10L);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        assertThrows(BadRequestException.class, () -> notificationService.markAsRead(10L, 999L));
    }

    @Test
    void testMarkAllAsRead() {
        when(notificationRepository.markAllAsReadForUser(5L)).thenReturn(4);
        int count = notificationService.markAllAsRead(5L);
        assertEquals(4, count);
    }
}
