package com.belajar.springboot.dp.dto;

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
public class TransferRequest {

    @NotBlank(message = "Nomor rekening asal wajib diisi")
    private String nomorRekeningAsal;

    @NotBlank(message = "Nomor rekening tujuan wajib diisi")
    private String nomorRekeningTujuan;

    @NotNull(message = "Nominal transfer wajib diisi")
    @Min(value = 10000, message = "Nominal transfer minimal Rp 10.000")
    private BigDecimal nominal;

    private String beritaTransfer;
}
