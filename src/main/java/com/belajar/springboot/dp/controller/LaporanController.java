package com.belajar.springboot.dp.controller;

import com.belajar.springboot.common.dto.WebResponse;
import com.belajar.springboot.dp.dto.LaporanHarianResponse;
import com.belajar.springboot.dp.service.LaporanHarianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/laporan")
public class LaporanController {

    @Autowired
    private LaporanHarianService laporanHarianService;

    @GetMapping("/harian")
    public ResponseEntity<WebResponse<LaporanHarianResponse>> getLaporanHarian(
            @RequestParam(value = "tanggal", required = false) String tanggal) {

        LocalDate tgl = (tanggal == null || tanggal.isBlank()) ? null : LocalDate.parse(tanggal);
        LaporanHarianResponse data = laporanHarianService.getLaporanHarian(tgl);

        WebResponse<LaporanHarianResponse> response = WebResponse.<LaporanHarianResponse>builder()
                .status("SUCCESS")
                .message("Laporan transaksi harian per " + data.getTanggal() + " berhasil dibuat")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}