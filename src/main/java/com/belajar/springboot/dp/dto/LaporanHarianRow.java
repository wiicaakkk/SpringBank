package com.belajar.springboot.dp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LaporanHarianRow {

    private LocalDateTime tanggal;
    private String nomorRekening;
    private String tipeTransaksi;
    private String deskripsi;
    private String operatorId;
    private BigDecimal debit;
    private BigDecimal kredit;
}