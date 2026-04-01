package monitor.service;

import monitor.entity.UserAuditLog;
import monitor.repository.UserAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final UserAuditLogRepository auditLogRepository;

    /**
     * Запись действия пользователя
     */
    @Transactional
    public void log(String userId, String username, String action,
                    String entityType, String entityId,
                    String oldValue, String newValue, String details,
                    String ipAddress, String userAgent) {

        UserAuditLog auditLog = UserAuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("📝 Аудит: {} - {} - {}", username, action, entityId);
    }

    /**
     * Упрощённая запись (без ip и userAgent)
     */
    @Transactional
    public void log(String username, String action,
                    String entityType, String entityId,
                    String oldValue, String newValue) {

        UserAuditLog auditLog = UserAuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Запись действия с деталями
     */
    @Transactional
    public void log(String username, String action,
                    String entityType, String entityId,
                    String oldValue, String newValue, String details) {

        UserAuditLog auditLog = UserAuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }
}