package com.belajar.springboot.auth.service;

import com.belajar.springboot.auth.dto.AuthResponse;
import com.belajar.springboot.auth.dto.LoginRequest;
import com.belajar.springboot.auth.entity.User;
import com.belajar.springboot.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username atau password salah!"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Username atau password salah!");
        }

        String dummyToken = "SESSION-TOKEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return AuthResponse.builder()
                .username(user.getUsername())
                .namaLengkap(user.getNamaLengkap())
                .nip(user.getNip())
                .role(user.getRole())
                .sessionToken(dummyToken)
                .build();
    }
}
