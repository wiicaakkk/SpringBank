package com.belajar.springboot.gl.dto;

import com.belajar.springboot.gl.entity.Coa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NeracaRow {

    private String kodeAkun;
    private String namaAkun;
    private BigDecimal saldo;

    public static NeracaRow dariCoa(Coa coa, BigDecimal saldo) {
        return new NeracaRow(coa.getKodeAkun(), coa.getNamaAkun(), saldo);
    }
}