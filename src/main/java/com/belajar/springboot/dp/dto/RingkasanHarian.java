package com.belajar.springboot.dp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RingkasanHarian {

    private String tipeTransaksi;
    private long jumlah;
    private BigDecimal totalDebit;
    private BigDecimal totalKredit;
}