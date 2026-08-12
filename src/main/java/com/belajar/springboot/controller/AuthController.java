package com.belajar.springboot.controller;

import com.belajar.springboot.dto.AuthResponse;
import com.belajar.springboot.dto.LoginRequest;
import com.belajar.springboot.dto.WebResponse;
import com.belajar.springboot.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<WebResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(new WebResponse<>("SUCCESS", "Login Berhasil", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new WebResponse<>("ERROR", e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<WebResponse<String>> logout() {
        return ResponseEntity.ok(new WebResponse<>("SUCCESS", "Logout Berhasil. Sesi diakhiri.", "LOGGED_OUT"));
    }
}
