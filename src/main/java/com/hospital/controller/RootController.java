package com.hospital.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping({"/", "/api/health"})
    public ResponseEntity<Map<String, String>> getRootStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Smart Hospital Management Backend is running"
        ));
    }
}
