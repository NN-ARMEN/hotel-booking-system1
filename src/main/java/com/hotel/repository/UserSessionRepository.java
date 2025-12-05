package com.hotel.repository;

import com.hotel.model.SessionStatus;
import com.hotel.model.User;
import com.hotel.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'REVOKED', s.revokedAt = :revokedAt " +
            "WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    int revokeAllActiveSessionsByUserId(@Param("userId") Long userId,
                                        @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'EXPIRED' " +
            "WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    int expireOldSessions(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId " +
            "ORDER BY s.createdAt DESC")
    List<UserSession> findUserSessionsHistory(@Param("userId") Long userId);
}