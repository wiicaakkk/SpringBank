package com.belajar.springboot.service;

import com.belajar.springboot.dto.AuthResponse;
import com.belajar.springboot.dto.LoginRequest;
import com.belajar.springboot.entity.User;
import com.belajar.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Username '" + request.getUsername() + "' tidak ditemukan!");
        }

        User user = userOpt.get();

        // Plain text matching for learning simplicity (can be upgraded to BCrypt)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Password salah untuk user '" + request.getUsername() + "'!");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatusUser())) {
            throw new RuntimeException("Akun user '" + request.getUsername() + "' dalam status " + user.getStatusUser() + "! Hubungi Admin.");
        }

        // Generate session token simulation
        String sessionToken = "BANK-TOKEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new AuthResponse(
                user.getUsername(),
                user.getNamaLengkap(),
                user.getNip(),
                user.getRole(),
                sessionToken,
                "Autentikasi Berhasil! Selamat datang " + user.getNamaLengkap()
        );
    }
}
