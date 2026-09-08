package com.belajar.springboot.sb.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoxSewaRequest {

    @NotBlank(message = "Nomor box wajib diisi")
    private String nomorBox;

    @NotEmpty(message = "CIF penyewa wajib diisi")
    private String cifPenyewa;

    @NotBlank(message = "Nama penyewa wajib diisi")
    private String namaPenyewa;

    private String nomorRekening;

    @NotNull(message = "Periode sewa (bulan) wajib diisi")
    @Min(value = 1, message = "Periode minimal 1 bulan")
    private Integer periodeBulan;

    @NotNull(message = "Biaya sewa wajib diisi")
    @Min(value = 1, message = "Biaya sewa harus lebih dari nol")
    private BigDecimal biayaSewa;
}