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
public class LabaRugiResponse {

    private LocalDate tanggal;
    private List<NeracaRow> pendapatan;
    private List<NeracaRow> beban;
    private BigDecimal totalPendapatan;
    private BigDecimal totalBeban;
    private BigDecimal labaBersih;
}