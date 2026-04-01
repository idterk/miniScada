package monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession(false);

            if (session != null) {
                SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (securityContext != null) {
                    Authentication auth = securityContext.getAuthentication();
                    if (auth != null && auth.isAuthenticated()) {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        attributes.put("user", auth.getPrincipal());
                        attributes.put("username", auth.getName());
                        attributes.put("sessionId", session.getId());
                        log.debug("✅ WebSocket подключён с сессией: {}, пользователь: {}",
                                session.getId(), auth.getName());
                        return true;
                    } else {
                        log.debug("WebSocket: сессия существует, но аутентификация отсутствует или невалидна");
                    }
                } else {
                    log.debug("WebSocket: в сессии нет SecurityContext");
                }
            } else {
                log.debug("WebSocket: нет активной сессии, используется JWT аутентификация");
            }
        }

        attributes.put("authenticated", false);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("Ошибка при handshake WebSocket: {}", exception.getMessage());
        }
        SecurityContextHolder.clearContext();
    }
}