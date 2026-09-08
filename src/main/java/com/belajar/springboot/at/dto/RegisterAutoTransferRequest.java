package com.belajar.springboot.at.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAutoTransferRequest {

    @NotBlank(message = "Rekening sumber (debit) wajib diisi")
    private String nomorRekeningDebit;

    @NotBlank(message = "Rekening tujuan (kredit) wajib diisi")
    private String nomorRekeningKredit;

    @NotNull(message = "Nominal auto transfer wajib diisi")
    @Min(value = 10000, message = "Nominal minimal Rp 10.000")
    private BigDecimal nominal;

    @NotNull(message = "Periode wajib diisi (HARIAN / MINGGUAN / BULANAN)")
    private String periode;

    private LocalDate tanggalMulai;
}