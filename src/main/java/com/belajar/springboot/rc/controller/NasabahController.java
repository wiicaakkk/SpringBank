package com.belajar.springboot.rc.controller;

import com.belajar.springboot.common.dto.WebResponse;
import com.belajar.springboot.rc.dto.NasabahResponse;
import com.belajar.springboot.rc.dto.RegisterNasabahRequest;
import com.belajar.springboot.rc.dto.UpdateNasabahRequest;
import com.belajar.springboot.rc.entity.NasabahHistory;
import com.belajar.springboot.rc.service.NasabahService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nasabah")
public class NasabahController {

    @Autowired
    private NasabahService nasabahService;

    @PostMapping("/register")
    public ResponseEntity<WebResponse<NasabahResponse>> register(
            @Valid @RequestBody RegisterNasabahRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {
        
        NasabahResponse responseData = nasabahService.register(request, operatorId);
        WebResponse<NasabahResponse> response = WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Nasabah berhasil didaftarkan ke sistem Core Banking!")
                .data(responseData)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<WebResponse<List<NasabahResponse>>> getAllNasabah() {
        List<NasabahResponse> list = nasabahService.getAllNasabah();
        WebResponse<List<NasabahResponse>> response = WebResponse.<List<NasabahResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil data nasabah")
                .data(list)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<WebResponse<List<NasabahResponse>>> searchNasabah(@RequestParam("keyword") String keyword) {
        List<NasabahResponse> list = nasabahService.searchNasabah(keyword);
        WebResponse<List<NasabahResponse>> response = WebResponse.<List<NasabahResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mencari data nasabah")
                .data(list)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cif}")
    public ResponseEntity<WebResponse<NasabahResponse>> getByCif(@PathVariable("cif") String cif) {
        NasabahResponse data = nasabahService.getNasabahByCif(cif);
        WebResponse<NasabahResponse> response = WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil data CIF " + cif)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cif}")
    public ResponseEntity<WebResponse<NasabahResponse>> updateNasabah(
            @PathVariable("cif") String cif,
            @Valid @RequestBody UpdateNasabahRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {
        
        NasabahResponse updated = nasabahService.updateNasabah(cif, request, operatorId);
        WebResponse<NasabahResponse> response = WebResponse.<NasabahResponse>builder()
                .status("SUCCESS")
                .message("Profil nasabah CIF " + cif + " berhasil diperbarui")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cif}/history")
    public ResponseEntity<WebResponse<List<NasabahHistory>>> getHistoryByCif(@PathVariable("cif") String cif) {
        List<NasabahHistory> history = nasabahService.getHistoryByCif(cif);
        WebResponse<List<NasabahHistory>> response = WebResponse.<List<NasabahHistory>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil audit history CIF " + cif)
                .data(history)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/all")
    public ResponseEntity<WebResponse<List<NasabahHistory>>> getAllHistory() {
        List<NasabahHistory> history = nasabahService.getAllHistory();
        WebResponse<List<NasabahHistory>> response = WebResponse.<List<NasabahHistory>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil seluruh audit history log")
                .data(history)
                .build();
        return ResponseEntity.ok(response);
    }
}
