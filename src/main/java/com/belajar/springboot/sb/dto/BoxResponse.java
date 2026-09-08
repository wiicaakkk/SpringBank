package com.belajar.springboot.sb.dto;

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
public class BoxResponse {

    private String nomorBox;
    private String status;
    private String cifPenyewa;
    private String namaPenyewa;
    private String nomorRekening;
    private LocalDate tanggalMulai;
    private LocalDate tanggalJatuhTempo;
    private BigDecimal tarifSewa;
}