package com.hotel.controller;

import com.hotel.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    @PreAuthorize("hasAnyRole('GUEST', 'ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<?> protectedEndpoint() {
        String username = SecurityUtils.getCurrentUsername();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to protected endpoint");
        response.put("username", username);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}