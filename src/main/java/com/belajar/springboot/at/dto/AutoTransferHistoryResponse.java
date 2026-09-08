package com.belajar.springboot.at.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoTransferHistoryResponse {

    private String kodeInstruksi;
    private BigDecimal nominal;
    private LocalDateTime tanggalEksekusi;
    private String statusEksekusi;
    private String keterangan;
    private String operatorEksekusi;
}