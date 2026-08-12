package com.belajar.springboot.dp.dto;

import com.belajar.springboot.dp.entity.JenisTabungan;
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
public class RekeningResponse {

    private String nomorRekening;

    private String cif;

    private String namaNasabah;

    private JenisTabungan jenisTabungan;

    private BigDecimal saldo;

    private String statusRekening;

    private LocalDateTime createdAt;
}
