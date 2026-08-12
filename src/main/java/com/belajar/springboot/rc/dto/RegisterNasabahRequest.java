package com.belajar.springboot.rc.dto;

import com.belajar.springboot.rc.entity.JenisKelamin;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterNasabahRequest {

    @NotBlank(message = "NIK wajib diisi")
    @Size(min = 16, max = 16, message = "NIK harus persis 16 digit")
    private String nik;

    @NotBlank(message = "Nama lengkap wajib diisi")
    private String namaLengkap;

    @NotBlank(message = "Tempat lahir wajib diisi")
    private String tempatLahir;

    @NotNull(message = "Tanggal lahir wajib diisi")
    private LocalDate tanggalLahir;

    @NotNull(message = "Jenis kelamin wajib diisi")
    private JenisKelamin jenisKelamin;

    @NotBlank(message = "Nama ibu kandung wajib diisi")
    private String ibuKandung;

    @NotBlank(message = "Alamat wajib diisi")
    private String alamat;

    @NotBlank(message = "Nomor HP wajib diisi")
    private String nomorHp;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Pekerjaan wajib diisi")
    private String pekerjaan;

    @NotNull(message = "Penghasilan bulanan wajib diisi")
    @PositiveOrZero(message = "Penghasilan tidak boleh negatif")
    private BigDecimal penghasilanBulanan;
}
