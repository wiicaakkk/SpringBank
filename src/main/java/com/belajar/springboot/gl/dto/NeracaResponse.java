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
public class NeracaResponse {

    private LocalDate tanggal;
    private List<NeracaRow> aktiva;
    private List<NeracaRow> pasiva;
    private BigDecimal totalAktiva;
    private BigDecimal totalPasiva;
    private BigDecimal selisih;
}