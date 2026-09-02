package com.belajar.springboot.dp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransaksiSummaryResponse {

    private Integer tahun;

    private Integer bulan;

    private String periode; // Contoh: "Feb 2025", "Mar 2025"

    private Long totalTransaksi;

    private BigDecimal totalNominal;

    private BigDecimal totalDebit;

    private BigDecimal totalKredit;
}
