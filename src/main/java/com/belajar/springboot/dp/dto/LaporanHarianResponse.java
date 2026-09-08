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
public class LaporanHarianResponse {

    private LocalDate tanggal;
    private List<LaporanHarianRow> baris;
    private List<RingkasanHarian> ringkasan;
    private long totalTransaksi;
    private BigDecimal totalDebit;
    private BigDecimal totalKredit;
}