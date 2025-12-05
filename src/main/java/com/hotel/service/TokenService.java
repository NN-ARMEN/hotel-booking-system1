package com.hotel.service;

import com.hotel.exception.TokenRefreshException;
import com.hotel.model.SessionStatus;
import com.hotel.model.User;
import com.hotel.model.UserSession;
import com.hotel.repository.UserRepository;
import com.hotel.repository.UserSessionRepository;
import com.hotel.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private Long refreshTokenExpirationMs;

    @Transactional
    public Map<String, Object> authenticateUser(String username, String password,
                                                HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Генерируем сессию
        String sessionId = tokenProvider.generateSessionId();
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, sessionId);

        // Сохраняем сессию в БД
        String refreshTokenHash = tokenProvider.hashRefreshToken(refreshToken);

        // Используем Duration для добавления миллисекунд
        LocalDateTime expiresAt = LocalDateTime.now().plus(
                Duration.ofMillis(refreshTokenExpirationMs)
        );

        UserSession session = new UserSession(
                sessionId,
                user,
                refreshTokenHash,
                expiresAt,
                getClientIp(request),
                request.getHeader("User-Agent")
        );

        userSessionRepository.save(session);

        // Отзываем старые сессии (опционально, для безопасности)
        revokeOldSessions(user.getId());

        // Формируем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("sessionId", sessionId);
        response.put("expiresIn", refreshTokenExpirationMs / 1000); // в секундах

        // User info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userDetails.getId());
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("email", userDetails.getEmail());

        response.put("user", userInfo);

        return response;
    }

    @Transactional
    public Map<String, Object> refreshToken(String refreshToken, HttpServletRequest request) {
        // Проверяем, что токен валиден и является refresh токеном
        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new TokenRefreshException("Invalid refresh token");
        }

        if (tokenProvider.isTokenExpired(refreshToken)) {
            throw new TokenRefreshException("Refresh token is expired");
        }

        String sessionId = tokenProvider.getSessionIdFromRefreshToken(refreshToken);
        String refreshTokenHash = tokenProvider.hashRefreshToken(refreshToken);

        // Ищем активную сессию
        UserSession session = userSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new TokenRefreshException("Session not found"));

        // Проверяем хэш токена
        if (!session.getRefreshTokenHash().equals(refreshTokenHash)) {
            session.markAsRevoked();
            userSessionRepository.save(session);
            throw new TokenRefreshException("Invalid refresh token");
        }

        // Проверяем статус сессии
        if (!session.isActive()) {
            throw new TokenRefreshException("Session is not active");
        }

        // Помечаем старую сессию как обновленную
        session.markAsRefreshed();
        userSessionRepository.save(session);

        // Создаем новую сессию
        User user = session.getUser();
        String newSessionId = tokenProvider.generateSessionId();

        // Аутентифицируем пользователя
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, userDetailsService.loadUserByUsername(user.getUsername()).getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерируем новые токены
        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication, newSessionId);

        // Сохраняем новую сессию
        String newRefreshTokenHash = tokenProvider.hashRefreshToken(newRefreshToken);
        LocalDateTime newExpiresAt = LocalDateTime.now().plus(
                Duration.ofMillis(refreshTokenExpirationMs)
        );

        UserSession newSession = new UserSession(
                newSessionId,
                user,
                newRefreshTokenHash,
                newExpiresAt,
                getClientIp(request),
                request.getHeader("User-Agent")
        );
        newSession.setLastRefreshedAt(LocalDateTime.now());

        userSessionRepository.save(newSession);

        // Формируем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("tokenType", "Bearer");
        response.put("sessionId", newSessionId);
        response.put("expiresIn", refreshTokenExpirationMs / 1000);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());

        response.put("user", userInfo);

        return response;
    }

    @Transactional
    public void revokeSession(String sessionId) {
        userSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.markAsRevoked();
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void revokeAllUserSessions(Long userId) {
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(userId);
        for (UserSession session : activeSessions) {
            session.markAsRevoked();
            userSessionRepository.save(session);
        }
    }

    @Transactional(readOnly = true)
    public List<UserSession> getUserSessions(Long userId) {
        return userSessionRepository.findUserSessionsHistory(userId);
    }

    @Scheduled(cron = "0 0 3 * * ?") // Ежедневно в 3:00
    @Transactional
    public void cleanupExpiredSessions() {
        List<UserSession> allSessions = userSessionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (UserSession session : allSessions) {
            if (session.getExpiresAt().isBefore(now) &&
                    (session.getStatus() == SessionStatus.ACTIVE || session.getStatus() == null)) {
                session.markAsExpired();
                userSessionRepository.save(session);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private void revokeOldSessions(Long userId) {
        // Оставляем только последние 5 активных сессий
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(userId);
        if (activeSessions.size() > 5) {
            // Сортируем по дате создания (новые сначала)
            activeSessions.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));

            // Оставляем первые 5, остальные отзываем
            for (int i = 5; i < activeSessions.size(); i++) {
                UserSession session = activeSessions.get(i);
                session.markAsRevoked();
                userSessionRepository.save(session);
            }
        }
    }
}