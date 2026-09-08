package com.belajar.springboot.gl.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JurnalRequest {

    @NotBlank(message = "Kode akun debit wajib diisi")
    private String kodeAkunDebit;

    @NotBlank(message = "Kode akun kredit wajib diisi")
    private String kodeAkunKredit;

    @NotNull(message = "Nominal wajib diisi")
    @Min(value = 1, message = "Nominal harus lebih dari nol")
    private BigDecimal nominal;

    private String keterangan;
}