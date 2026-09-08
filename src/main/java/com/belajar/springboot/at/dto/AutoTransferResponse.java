package com.belajar.springboot.at.dto;

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
public class AutoTransferResponse {

    private String kodeInstruksi;
    private String nomorRekeningDebit;
    private String nomorRekeningKredit;
    private BigDecimal nominal;
    private String periode;
    private LocalDate tanggalMulai;
    private LocalDate tanggalBerikutnya;
    private String statusAt;
    private String operatorDibuat;
    private LocalDateTime createdAt;
}