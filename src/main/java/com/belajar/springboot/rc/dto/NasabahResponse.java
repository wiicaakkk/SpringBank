package com.belajar.springboot.rc.dto;

import com.belajar.springboot.rc.entity.JenisKelamin;
import com.belajar.springboot.rc.entity.StatusNasabah;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NasabahResponse implements Serializable {

    private String cif;

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
