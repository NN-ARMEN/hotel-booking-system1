// File: src/main/java/com/hotel/security/JwtTokenProvider.java
package com.hotel.security;

import com.hotel.service.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:defaultSecretKeyForJWTTokenGeneration1234567890}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms:900000}")
    private Long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private Long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA512");
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Простая реализация без использования внешней библиотеки
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS512\",\"typ\":\"JWT\"}".getBytes());

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userPrincipal.getUsername());
        payload.put("userId", userPrincipal.getId());
        payload.put("email", userPrincipal.getEmail());
        payload.put("tokenType", "ACCESS");
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() + accessTokenExpirationMs) / 1000);

        String payloadJson = "{\"sub\":\"" + userPrincipal.getUsername() + "\"," +
                "\"userId\":" + userPrincipal.getId() + "," +
                "\"email\":\"" + userPrincipal.getEmail() + "\"," +
                "\"tokenType\":\"ACCESS\"," +
                "\"iat\":" + (System.currentTimeMillis() / 1000) + "," +
                "\"exp\":" + ((System.currentTimeMillis() + accessTokenExpirationMs) / 1000) + "}";

        String payloadBase64 = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());

        String signature = generateSignature(header + "." + payloadBase64);

        return header + "." + payloadBase64 + "." + signature;
    }

    public String generateRefreshToken(Authentication authentication, String sessionId) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS512\",\"typ\":\"JWT\"}".getBytes());

        String payloadJson = "{\"sub\":\"" + userPrincipal.getUsername() + "\"," +
                "\"userId\":" + userPrincipal.getId() + "," +
                "\"tokenType\":\"REFRESH\"," +
                "\"sessionId\":\"" + sessionId + "\"," +
                "\"iat\":" + (System.currentTimeMillis() / 1000) + "," +
                "\"exp\":" + ((System.currentTimeMillis() + refreshTokenExpirationMs) / 1000) + "}";

        String payloadBase64 = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());

        String signature = generateSignature(header + "." + payloadBase64);

        return header + "." + payloadBase64 + "." + signature;
    }

    private String generateSignature(String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            mac.init(getSigningKey());
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    public Map<String, Object> validateAndGetClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid token format");
            }

            String signature = generateSignature(parts[0] + "." + parts[1]);
            if (!signature.equals(parts[2])) {
                throw new RuntimeException("Invalid signature");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Простой парсинг JSON (в реальном проекте используйте Jackson/Gson)
            return parseSimpleJson(payloadJson);

        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    private Map<String, Object> parseSimpleJson(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.trim().substring(1, json.length() - 1);
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim();

                if (value.startsWith("\"") && value.endsWith("\"")) {
                    map.put(key, value.substring(1, value.length() - 1));
                } else if (value.matches("\\d+")) {
                    map.put(key, Long.parseLong(value));
                } else if (value.equals("true") || value.equals("false")) {
                    map.put(key, Boolean.parseBoolean(value));
                } else {
                    map.put(key, value);
                }
            }
        }

        return map;
    }

    public String getUsernameFromToken(String token) {
        Map<String, Object> claims = validateAndGetClaims(token);
        return (String) claims.get("sub");
    }

    public Long getUserIdFromToken(String token) {
        Map<String, Object> claims = validateAndGetClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    public String getTokenType(String token) {
        Map<String, Object> claims = validateAndGetClaims(token);
        return (String) claims.get("tokenType");
    }

    public String getSessionIdFromRefreshToken(String token) {
        Map<String, Object> claims = validateAndGetClaims(token);
        if (!"REFRESH".equals(claims.get("tokenType"))) {
            throw new RuntimeException("Not a refresh token");
        }
        return (String) claims.get("sessionId");
    }

    public boolean isAccessToken(String token) {
        try {
            return "ACCESS".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Map<String, Object> claims = validateAndGetClaims(token);
            Long exp = (Long) claims.get("exp");
            return exp * 1000 < System.currentTimeMillis();
        } catch (Exception e) {
            return true;
        }
    }

    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    public String hashRefreshToken(String refreshToken) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return Integer.toHexString(refreshToken.hashCode());
        }
    }
}