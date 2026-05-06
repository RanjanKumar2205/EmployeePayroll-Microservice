package org.example.notificationservice.services;

import org.example.notificationservice.dtos.NotificationRequestDto;
import org.example.notificationservice.dtos.NotificationResponseDto;
import org.example.notificationservice.entities.Notification;
import org.example.notificationservice.entities.NotificationStatus;
import org.example.notificationservice.exceptions.ResourceNotFoundException;
import org.example.notificationservice.mappers.NotificationMapper;
import org.example.notificationservice.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    public Page<NotificationResponseDto> getAll(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(notificationMapper::toResponse);
    }

    public List<NotificationResponseDto> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public NotificationResponseDto getById(Long id) {
        return notificationMapper.toResponse(findById(id));
    }

    /**
     * Persist the notification and immediately attempt a simulated send.
     * In a real system this would publish to a queue (Kafka / RabbitMQ).
     */
    @Transactional
    public NotificationResponseDto send(NotificationRequestDto dto) {
        Notification n = notificationRepository.save(notificationMapper.toEntity(dto));

        try {
            log.info("[notification-service] Sending {} notification to {} — subject: {}",
                    n.getChannel(), n.getRecipientEmail(), n.getSubject());
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(LocalDateTime.now());
        } catch (Exception ex) {
            n.setStatus(NotificationStatus.FAILED);
            n.setErrorMessage(ex.getMessage());
            log.error("[notification-service] Failed to send notification id={}", n.getId(), ex);
        }

        return notificationMapper.toResponse(notificationRepository.save(n));
    }

    private Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }
}
