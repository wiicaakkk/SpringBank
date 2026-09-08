package com.belajar.springboot.gl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BukuBesarRow {

    private String kodeAkun;
    private String namaAkun;
    private LocalDate tanggal;
    private String sisi;
    private BigDecimal nominal;
    private String refTransaksi;
    private String keterangan;
    private String operatorId;
    private BigDecimal saldo;
}