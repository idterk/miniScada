package monitor.service;

import monitor.dto.request.LoginRequest;
import monitor.dto.response.AuthResponse;
import monitor.entity.User;
import monitor.entity.UserAuditLog;
import monitor.repository.UserRepository;
import monitor.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthResponse authenticate(LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // СОЗДАЁМ И СОХРАНЯЕМ СЕССИЮ В REDIS
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        session.setAttribute("username", loginRequest.getUsername());
        session.setAttribute("authenticated", true);
        session.setMaxInactiveInterval(3600); // 1 час

        log.info("✅ Сессия создана: id={}, username={}, будет храниться в Redis",
                session.getId(), loginRequest.getUsername());

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Обновляем время последнего входа
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Аудит входа
        auditService.log(
                user.getId(),
                user.getUsername(),
                UserAuditLog.ACTION_LOGIN,
                "USER",
                user.getId(),
                null,
                null,
                "User logged in. Session: " + session.getId(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        log.info("👤 User logged in: {}, sessionId={}", user.getUsername(), session.getId());

        return new AuthResponse(jwt, user.getUsername(), user.getRole().name());
    }
}