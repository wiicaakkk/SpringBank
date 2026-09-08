package com.belajar.springboot.dp.dto;

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
public class MutasiResponse {

    private String nomorRekening;
    private String namaNasabah;
    private BigDecimal saldoAkhir;
    private LocalDate tanggalDari;
    private LocalDate tanggalSampai;
    private List<MutasiRowResponse> baris;
    private BigDecimal totalDebit;
    private BigDecimal totalKredit;
}