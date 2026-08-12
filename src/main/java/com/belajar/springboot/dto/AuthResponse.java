package com.belajar.springboot.dto;

import com.belajar.springboot.entity.Role;

public class AuthResponse {
    private String username;
    private String namaLengkap;
    private String nip;
    private Role role;
    private String token;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String username, String namaLengkap, String nip, Role role, String token, String message) {
        this.username = username;
        this.namaLengkap = namaLengkap;
        this.nip = nip;
        this.role = role;
        this.token = token;
        this.message = message;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
