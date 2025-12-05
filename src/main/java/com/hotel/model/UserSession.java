package com.hotel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public UserSession() {}

    public UserSession(String sessionId, User user, String refreshTokenHash,
                       LocalDateTime expiresAt, String ipAddress, String userAgent) {
        this.sessionId = sessionId;
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastRefreshedAt() { return lastRefreshedAt; }
    public void setLastRefreshedAt(LocalDateTime lastRefreshedAt) { this.lastRefreshedAt = lastRefreshedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE &&
                LocalDateTime.now().isBefore(expiresAt);
    }

    public void markAsRefreshed() {
        this.status = SessionStatus.REFRESHED;
        this.revokedAt = LocalDateTime.now();
    }

    public void markAsRevoked() {
        this.status = SessionStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = SessionStatus.EXPIRED;
    }
}