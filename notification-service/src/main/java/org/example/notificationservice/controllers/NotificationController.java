package org.example.notificationservice.controllers;

import jakarta.validation.Valid;
import org.example.notificationservice.dtos.NotificationRequestDto;
import org.example.notificationservice.dtos.NotificationResponseDto;
import org.example.notificationservice.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAll(pageable));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<NotificationResponseDto>> getByRecipient(
            @PathVariable Long recipientId) {
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDto> send(
            @Valid @RequestBody NotificationRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.send(dto));
    }
}
