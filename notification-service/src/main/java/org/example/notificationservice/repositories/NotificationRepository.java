package org.example.notificationservice.repositories;

import org.example.notificationservice.entities.Notification;
import org.example.notificationservice.entities.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientId(Long recipientId);

    Page<Notification> findAllByStatus(NotificationStatus status, Pageable pageable);
}
