package com.belajar.springboot.dto;

import com.belajar.springboot.entity.JenisKelamin;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterNasabahRequest {

    @NotBlank(message = "NIK wajib diisi")
    @Size(min = 16, max = 16, message = "NIK harus persis 16 digit angka")
    @Pattern(regexp = "^[0-9]+$", message = "NIK hanya boleh berisi angka")
    private String nik;

    @NotBlank(message = "Nama lengkap wajib diisi")
    private String namaLengkap;

    @NotBlank(message = "Tempat lahir wajib diisi")
    private String tempatLahir;

    @NotNull(message = "Tanggal lahir wajib diisi")
    @Past(message = "Tanggal lahir harus tanggal di masa lalu")
    private LocalDate tanggalLahir;

    @NotNull(message = "Jenis kelamin wajib diisi")
    private JenisKelamin jenisKelamin;

    @NotBlank(message = "Nama ibu kandung wajib diisi untuk verifikasi keamanan bank")
    private String ibuKandung;

    @NotBlank(message = "Alamat domisili wajib diisi")
    private String alamat;

    @NotBlank(message = "Nomor HP wajib diisi")
    @Pattern(regexp = "^[0-9\\+\\-\\s]+$", message = "Nomor HP tidak valid")
    private String nomorHp;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Pekerjaan wajib diisi")
    private String pekerjaan;

    @NotNull(message = "Penghasilan bulanan wajib diisi")
    @Min(value = 0, message = "Penghasilan tidak boleh bernilai negatif")
    private BigDecimal penghasilanBulanan;
}
