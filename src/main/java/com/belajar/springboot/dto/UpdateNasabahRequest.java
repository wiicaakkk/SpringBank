package com.belajar.springboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNasabahRequest {

    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    private String namaLengkap;

    @Size(max = 255, message = "Alamat maksimal 255 karakter")
    private String alamat;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Nomor HP harus berupa angka 10-15 digit")
    private String nomorHp;

    @Email(message = "Format email tidak valid")
    private String email;

    @Size(max = 50, message = "Pekerjaan maksimal 50 karakter")
    private String pekerjaan;

    @Min(value = 0, message = "Penghasilan bulanan tidak boleh negatif")
    private BigDecimal penghasilanBulanan;
}
