package com.hotel.controller;

import com.hotel.dto.AuthDTO;
import com.hotel.model.User;
import com.hotel.service.AuthService;
import com.hotel.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthDTO.LoginRequest loginRequest,
                                              HttpServletRequest request) {
        try {
            Map<String, Object> tokens = tokenService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword(),
                    request
            );

            AuthDTO.TokenResponse response = new AuthDTO.TokenResponse();
            response.setAccessToken((String) tokens.get("accessToken"));
            response.setRefreshToken((String) tokens.get("refreshToken"));
            response.setTokenType((String) tokens.get("tokenType"));
            response.setSessionId((String) tokens.get("sessionId"));
            response.setExpiresIn((Long) tokens.get("expiresIn"));
            response.setUser(tokens.get("user"));

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody AuthDTO.TokenRefreshRequest refreshRequest,
                                          HttpServletRequest request) {
        try {
            Map<String, Object> tokens = tokenService.refreshToken(
                    refreshRequest.getRefreshToken(),
                    request
            );

            AuthDTO.TokenResponse response = new AuthDTO.TokenResponse();
            response.setAccessToken((String) tokens.get("accessToken"));
            response.setRefreshToken((String) tokens.get("refreshToken"));
            response.setTokenType((String) tokens.get("tokenType"));
            response.setSessionId((String) tokens.get("sessionId"));
            response.setExpiresIn((Long) tokens.get("expiresIn"));
            response.setUser(tokens.get("user"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String sessionId) {
        tokenService.revokeSession(sessionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestParam Long userId) {
        tokenService.revokeAllUserSessions(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All sessions logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthDTO.RegisterRequest registerRequest) {
        try {
            User user = authService.registerUser(registerRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !authService.existsByUsername(username));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !authService.existsByEmail(email));
        return ResponseEntity.ok(response);
    }
}