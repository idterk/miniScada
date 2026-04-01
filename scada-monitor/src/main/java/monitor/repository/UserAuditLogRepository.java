package monitor.repository;

import monitor.entity.UserAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {

    Page<UserAuditLog> findByUserId(String userId, Pageable pageable);

    // Добавляем этот метод
    Page<UserAuditLog> findByUsername(String username, Pageable pageable);

    Page<UserAuditLog> findByAction(String action, Pageable pageable);

    Page<UserAuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<UserAuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
}