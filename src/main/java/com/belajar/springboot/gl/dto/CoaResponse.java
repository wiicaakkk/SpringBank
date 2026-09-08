package com.belajar.springboot.gl.dto;

import com.belajar.springboot.gl.entity.JenisAkun;
import com.belajar.springboot.gl.entity.PosisiNormal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoaResponse {

    private Long id;
    private String kodeAkun;
    private String namaAkun;
    private JenisAkun jenisAkun;
    private PosisiNormal posisiNormal;
    private Boolean aktif;
    private BigDecimal saldo;
}