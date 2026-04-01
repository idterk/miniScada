package monitor.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SessionController {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Получить все активные сессии из Redis
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSessions() {
        Map<String, Object> result = new HashMap<>();

        try {
            Set<String> sessionKeys = redisTemplate.keys("spring:session:sessions:*");

            if (sessionKeys != null && !sessionKeys.isEmpty()) {
                result.put("totalSessions", sessionKeys.size());
                result.put("sessionKeys", sessionKeys);

                Map<String, Object> sessionDetails = new HashMap<>();
                for (String key : sessionKeys) {
                    Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                    sessionDetails.put(key, entries);
                }
                result.put("sessions", sessionDetails);
            } else {
                result.put("totalSessions", 0);
                result.put("message", "Нет активных сессий в Redis");
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при получении сессий из Redis", e);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Удалить сессию из Redis
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> invalidateSession(@PathVariable String sessionId) {
        try {
            String key = "spring:session:sessions:" + sessionId;
            Boolean deleted = redisTemplate.delete(key);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("🗑️ Сессия удалена из Redis: {}", sessionId);
                return ResponseEntity.ok(Map.of(
                        "status", "deleted",
                        "sessionId", sessionId
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении сессии", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Проверить текущую сессию
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSession(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("creationTime", session.getCreationTime());
        result.put("lastAccessedTime", session.getLastAccessedTime());
        result.put("maxInactiveInterval", session.getMaxInactiveInterval());
        result.put("isNew", session.isNew());

        // Получаем атрибуты сессии
        Map<String, Object> attributes = new HashMap<>();
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            attributes.put(name, session.getAttribute(name));
        }
        result.put("attributes", attributes);

        return ResponseEntity.ok(result);
    }
}