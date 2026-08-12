package com.belajar.springboot.auth.dto;

import com.belajar.springboot.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String username;

    private String namaLengkap;

    private String nip;

    private Role role;

    private String sessionToken;
}
