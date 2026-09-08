package com.belajar.springboot.gl.dto;

import com.belajar.springboot.gl.entity.JenisAkun;
import com.belajar.springboot.gl.entity.PosisiNormal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCoaRequest {

    @NotBlank(message = "Kode akun wajib diisi")
    private String kodeAkun;

    @NotBlank(message = "Nama akun wajib diisi")
    private String namaAkun;

    @NotNull(message = "Jenis akun wajib diisi (ASET / KEWAJIBAN / EKUITAS)")
    private JenisAkun jenisAkun;

    @NotNull(message = "Posisi normal wajib diisi (DEBIT / KREDIT)")
    private PosisiNormal posisiNormal;

    private Boolean aktif;
}