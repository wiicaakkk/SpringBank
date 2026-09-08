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
public class CashOperationRequest {

    @NotBlank(message = "Nomor rekening wajib diisi")
    private String nomorRekening;

    @NotNull(message = "Nominal wajib diisi")
    @Min(value = 10000, message = "Nominal minimal Rp 10.000")
    private BigDecimal nominal;
}