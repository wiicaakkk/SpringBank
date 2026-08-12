package com.belajar.springboot.dto;

import com.belajar.springboot.entity.JenisKelamin;
import com.belajar.springboot.entity.StatusNasabah;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NasabahResponse {

    private Long id;
    private String cif; // Customer Information File
    private String nik;
    private String namaLengkap;
    private String tempatLahir;
    private LocalDate tanggalLahir;
    private JenisKelamin jenisKelamin;
    private String ibuKandung;
    private String alamat;
    private String nomorHp;
    private String email;
    private String pekerjaan;
    private BigDecimal penghasilanBulanan;
    private StatusNasabah statusNasabah;
    private LocalDateTime createdAt;
}
