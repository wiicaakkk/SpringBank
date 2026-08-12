package com.belajar.springboot.rc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNasabahRequest {

    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    private String namaLengkap;

    @NotBlank(message = "Alamat tidak boleh kosong")
    private String alamat;

    @NotBlank(message = "Nomor HP tidak boleh kosong")
    private String nomorHp;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Pekerjaan tidak boleh kosong")
    private String pekerjaan;

    @NotNull(message = "Penghasilan bulanan tidak boleh kosong")
    @PositiveOrZero(message = "Penghasilan tidak boleh negatif")
    private BigDecimal penghasilanBulanan;
}
