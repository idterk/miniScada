package monitor.controller;

import monitor.entity.ErrorLog;
import monitor.entity.UserAuditLog;
import monitor.repository.ErrorLogRepository;
import monitor.repository.UserAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final UserAuditLogRepository auditLogRepository;
    private final ErrorLogRepository errorLogRepository;

    /**
     * Получить последние действия пользователей
     */
    @GetMapping("/actions")
    public ResponseEntity<Page<UserAuditLog>> getActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(auditLogRepository.findAll(pageable));
    }

    /**
     * Получить действия по имени пользователя
     */
    @GetMapping("/actions/user/{username}")
    public ResponseEntity<Page<UserAuditLog>> getUserActions(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Используем findByUsername
        return ResponseEntity.ok(auditLogRepository.findByUsername(username, pageable));
    }

    /**
     * Получить последние ошибки
     */
    @GetMapping("/errors")
    public ResponseEntity<Page<ErrorLog>> getErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) ErrorLog.ErrorType errorType) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (errorType != null) {
            return ResponseEntity.ok(errorLogRepository.findByErrorType(errorType, pageable));
        }
        return ResponseEntity.ok(errorLogRepository.findAll(pageable));
    }

    /**
     * Получить ошибки для конкретного датчика
     */
    @GetMapping("/errors/sensor/{sensorId}")
    public ResponseEntity<Page<ErrorLog>> getSensorErrors(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(errorLogRepository.findBySensorId(sensorId, pageable));
    }

    /**
     * Статистика ошибок
     */
    @GetMapping("/errors/stats")
    public ResponseEntity<Map<String, Long>> getErrorStats() {
        return ResponseEntity.ok(Map.of(
                "total", errorLogRepository.count()
        ));
    }
}