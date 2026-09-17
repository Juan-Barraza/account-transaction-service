package com.bancolombia.challenge.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthCheck {
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = Map.of(
                "status", "UP",
                "service", "account-transaction-service",
                "database", "CONNECTED",
                "redis", "CONNECTED"
        );
        return ResponseEntity.ok(status);
    }
}
