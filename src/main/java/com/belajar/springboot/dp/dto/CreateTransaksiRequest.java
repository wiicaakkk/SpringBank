package com.belajar.springboot.dp.dto;

import com.belajar.springboot.dp.entity.TipeTransaksi;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateTransaksiRequest {

    @NotNull(message = "Nomor rekening tidak boleh kosong")
    private String nomorRekening;

    @NotNull(message = "Tipe transaksi tidak boleh kosong")
    private TipeTransaksi tipeTransaksi;

    @NotNull(message = "Amount tidak boleh kosong")
    @Positive(message = "Amount harus lebih besar dari 0")
    private BigDecimal amount;

    private String deskripsi;

    private LocalDateTime createdAt;
}
