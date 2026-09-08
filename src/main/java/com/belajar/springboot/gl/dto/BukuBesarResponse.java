package com.belajar.springboot.gl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BukuBesarResponse {

    private LocalDate tanggalDari;
    private LocalDate tanggalSampai;
    private String kodeAkun;
    private String namaAkun;
    private List<BukuBesarRow> baris;
    private BigDecimal totalDebit;
    private BigDecimal totalKredit;
    private BigDecimal saldoAkhir;
}