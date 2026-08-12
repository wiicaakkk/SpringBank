package com.belajar.springboot.dp.dto;

import com.belajar.springboot.dp.entity.JenisTabungan;
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
public class CreateRekeningRequest {

    @NotBlank(message = "Nomor CIF tidak boleh kosong")
    private String cif;

    @NotNull(message = "Jenis tabungan tidak boleh kosong")
    private JenisTabungan jenisTabungan;

    @NotNull(message = "Setoran awal tidak boleh kosong")
    @Min(value = 50000, message = "Setoran awal minimal Rp 50.000")
    private BigDecimal setoranAwal;
}
