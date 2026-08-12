package com.belajar.springboot.auth.controller;

import com.belajar.springboot.auth.dto.AuthResponse;
import com.belajar.springboot.auth.dto.LoginRequest;
import com.belajar.springboot.auth.service.AuthService;
import com.belajar.springboot.common.dto.WebResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<WebResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        WebResponse<AuthResponse> response = WebResponse.<AuthResponse>builder()
                .status("SUCCESS")
                .message("Login Berhasil! Selamat Datang " + authResponse.getNamaLengkap())
                .data(authResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<WebResponse<String>> logout() {
        WebResponse<String> response = WebResponse.<String>builder()
                .status("SUCCESS")
                .message("Logout Berhasil")
                .data("SESSION_TERMINATED")
                .build();
        return ResponseEntity.ok(response);
    }
}
