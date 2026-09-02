package com.belajar.springboot.dp.controller;

import com.belajar.springboot.common.dto.WebResponse;
import com.belajar.springboot.dp.dto.CreateTransaksiRequest;
import com.belajar.springboot.dp.dto.TransaksiSummaryResponse;
import com.belajar.springboot.dp.entity.Transaksi;
import com.belajar.springboot.dp.service.TransaksiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaksi")
@RequiredArgsConstructor
public class TransaksiController {

    private final TransaksiService transaksiService;

    @GetMapping("/summary")
    public ResponseEntity<WebResponse<List<TransaksiSummaryResponse>>> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String nomorRekening
    ) {
        List<TransaksiSummaryResponse> summary = transaksiService.getMonthlySummary(year, nomorRekening);
        return ResponseEntity.ok(
                WebResponse.<List<TransaksiSummaryResponse>>builder()
                        .status("OK")
                        .message("Berhasil mengambil summary transaksi bulanan")
                        .data(summary)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<WebResponse<List<Transaksi>>> getHistory(
            @RequestParam(required = false) String nomorRekening
    ) {
        List<Transaksi> history = transaksiService.getHistory(nomorRekening);
        return ResponseEntity.ok(
                WebResponse.<List<Transaksi>>builder()
                        .status("OK")
                        .message("Berhasil mengambil data transaksi")
                        .data(history)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<WebResponse<Transaksi>> createTransaksi(
            @Valid @RequestBody CreateTransaksiRequest request
    ) {
        Transaksi created = transaksiService.createTransaksi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                WebResponse.<Transaksi>builder()
                        .status("CREATED")
                        .message("Transaksi berhasil dibuat")
                        .data(created)
                        .build()
        );
    }
}
