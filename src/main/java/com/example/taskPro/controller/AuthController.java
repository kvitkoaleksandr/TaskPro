package com.example.taskPro.controller;

import com.example.taskPro.dto.securityDto.AuthRequestDto;
import com.example.taskPro.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.register(request.getEmail(), request.getPassword(), request.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }
}