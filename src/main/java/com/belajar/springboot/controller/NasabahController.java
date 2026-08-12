package com.belajar.springboot.controller;

import com.belajar.springboot.dto.NasabahResponse;
import com.belajar.springboot.dto.RegisterNasabahRequest;
import com.belajar.springboot.dto.UpdateNasabahRequest;
import com.belajar.springboot.dto.WebResponse;
import com.belajar.springboot.entity.NasabahHistory;
import com.belajar.springboot.entity.StatusNasabah;
import com.belajar.springboot.service.NasabahService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nasabah")
public class NasabahController {

    private final NasabahService nasabahService;

    @Autowired
    public NasabahController(NasabahService nasabahService) {
        this.nasabahService = nasabahService;
    }

    /**
     * Endpoint Pendaftaran Nasabah Baru (Pembuatan CIF Otomatis)
     */
    @PostMapping("/register")
    public ResponseEntity<WebResponse<NasabahResponse>> registerNasabah(
            @Valid @RequestBody RegisterNasabahRequest request) {
        NasabahResponse data = nasabahService.registerNasabah(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Pendaftaran Nasabah berhasil! Nomor CIF berhasil diterbitkan.")
                .data(data)
                .build());
    }

    /**
     * Ambil daftar seluruh Nasabah Bank
     */
    @GetMapping
    public ResponseEntity<WebResponse<List<NasabahResponse>>> getAllNasabah() {
        List<NasabahResponse> data = nasabahService.getAllNasabah();
        return ResponseEntity.ok(WebResponse.<List<NasabahResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil data seluruh nasabah")
                .data(data)
                .build());
    }

    /**
     * Cari detail Nasabah berdasarkan Nomor CIF
     */
    @GetMapping("/{cif}")
    public ResponseEntity<WebResponse<NasabahResponse>> getNasabahByCif(@PathVariable String cif) {
        NasabahResponse data = nasabahService.getNasabahByCif(cif);
        return ResponseEntity.ok(WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Data Nasabah ditemukan untuk CIF: " + cif)
                .data(data)
                .build());
    }

    /**
     * Pencarian Nasabah berdasarkan keyword (CIF, NIK, atau Nama)
     */
    @GetMapping("/search")
    public ResponseEntity<WebResponse<List<NasabahResponse>>> searchNasabah(@RequestParam("keyword") String keyword) {
        List<NasabahResponse> data = nasabahService.searchNasabah(keyword);
        return ResponseEntity.ok(WebResponse.<List<NasabahResponse>>builder()
                .status("SUCCESS")
                .message("Hasil pencarian untuk kata kunci: " + keyword)
                .data(data)
                .build());
    }

    /**
     * Update data profil nasabah
     */
    @PutMapping("/{cif}")
    public ResponseEntity<WebResponse<NasabahResponse>> updateNasabah(
            @PathVariable String cif,
            @Valid @RequestBody UpdateNasabahRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false) String operatorId) {
        NasabahResponse data = nasabahService.updateNasabah(cif, request, operatorId);
        return ResponseEntity.ok(WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Berhasil memperbarui profil nasabah CIF: " + cif)
                .data(data)
                .build());
    }

    /**
     * Ubah Status Nasabah (AKTIF / BLOCKED / PENDING_VERIFIKASI)
     */
    @PatchMapping("/{cif}/status")
    public ResponseEntity<WebResponse<NasabahResponse>> updateStatusNasabah(@PathVariable String cif,
            @RequestParam("status") StatusNasabah status) {
        NasabahResponse data = nasabahService.updateStatusNasabah(cif, status);
        return ResponseEntity.ok(WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Berhasil mengubah status nasabah CIF: " + cif + " menjadi " + status)
                .data(data)
                .build());
    }

    /**
     * Ambil Riwayat Audit Trail Perubahan Nasabah berdasarkan Nomor CIF
     */
    @GetMapping("/{cif}/history")
    public ResponseEntity<WebResponse<List<NasabahHistory>>> getNasabahHistory(@PathVariable String cif) {
        List<NasabahHistory> data = nasabahService.getNasabahHistory(cif);
        return ResponseEntity.ok(WebResponse.<List<NasabahHistory>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil riwayat Audit Trail untuk CIF: " + cif)
                .data(data)
                .build());
    }

    /**
     * Ambil seluruh Audit Log sistem perbankan
     */
    @GetMapping("/history/all")
    public ResponseEntity<WebResponse<List<NasabahHistory>>> getAllHistory() {
        List<NasabahHistory> data = nasabahService.getAllHistory();
        return ResponseEntity.ok(WebResponse.<List<NasabahHistory>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil seluruh riwayat Audit Trail perbankan")
                .data(data)
                .build());
    }
}
