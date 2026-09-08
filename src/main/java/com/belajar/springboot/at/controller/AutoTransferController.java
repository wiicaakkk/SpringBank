package com.belajar.springboot.at.controller;

import com.belajar.springboot.at.dto.AutoTransferHistoryResponse;
import com.belajar.springboot.at.dto.AutoTransferResponse;
import com.belajar.springboot.at.dto.ExecuteAutoTransferResponse;
import com.belajar.springboot.at.dto.RegisterAutoTransferRequest;
import com.belajar.springboot.at.service.AutoTransferService;
import com.belajar.springboot.common.dto.WebResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auto-transfer")
public class AutoTransferController {

    @Autowired
    private AutoTransferService autoTransferService;

    @PostMapping("/register")
    public ResponseEntity<WebResponse<AutoTransferResponse>> register(
            @Valid @RequestBody RegisterAutoTransferRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "SYSTEM") String operatorId) {

        AutoTransferResponse data = autoTransferService.register(request, operatorId);
        WebResponse<AutoTransferResponse> response = WebResponse.<AutoTransferResponse>builder()
                .status("SUCCESS")
                .message("Instruksi auto transfer " + data.getKodeInstruksi() + " berhasil terdaftar!")
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<WebResponse<List<AutoTransferResponse>>> list(
            @RequestParam(value = "status", required = false) String status) {

        List<AutoTransferResponse> data = autoTransferService.list(status);
        WebResponse<List<AutoTransferResponse>> response = WebResponse.<List<AutoTransferResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil daftar instrumen auto transfer")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{kodeInstruksi}/stop")
    public ResponseEntity<WebResponse<AutoTransferResponse>> stop(
            @PathVariable("kodeInstruksi") String kodeInstruksi,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "SYSTEM") String operatorId) {

        AutoTransferResponse data = autoTransferService.stop(kodeInstruksi, operatorId);
        WebResponse<AutoTransferResponse> response = WebResponse.<AutoTransferResponse>builder()
                .status("SUCCESS")
                .message("Instruksi " + kodeInstruksi + " dihentikan.")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{kodeInstruksi}/start")
    public ResponseEntity<WebResponse<AutoTransferResponse>> start(
            @PathVariable("kodeInstruksi") String kodeInstruksi,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "SYSTEM") String operatorId) {

        AutoTransferResponse data = autoTransferService.start(kodeInstruksi, operatorId);
        WebResponse<AutoTransferResponse> response = WebResponse.<AutoTransferResponse>builder()
                .status("SUCCESS")
                .message("Instruksi " + kodeInstruksi + " diaktifkan kembali.")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{kodeInstruksi}/history")
    public ResponseEntity<WebResponse<List<AutoTransferHistoryResponse>>> history(
            @PathVariable("kodeInstruksi") String kodeInstruksi) {

        List<AutoTransferHistoryResponse> data = autoTransferService.history(kodeInstruksi);
        WebResponse<List<AutoTransferHistoryResponse>> response = WebResponse.<List<AutoTransferHistoryResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil riwayat eksekusi " + kodeInstruksi)
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/execute")
    public ResponseEntity<WebResponse<ExecuteAutoTransferResponse>> execute(
            @RequestBody(required = false) ExecuteRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "SYSTEM") String operatorId) {

        String kode = request != null ? request.getKodeInstruksi() : null;
        ExecuteAutoTransferResponse data = autoTransferService.execute(kode, operatorId);
        WebResponse<ExecuteAutoTransferResponse> response = WebResponse.<ExecuteAutoTransferResponse>builder()
                .status("SUCCESS")
                .message(String.format("Eksekusi selesai: %d terproses, %d sukses, %d gagal",
                        data.getTerproses(), data.getSukses(), data.getGagal()))
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    public static class ExecuteRequest {
        private String kodeInstruksi;

        public String getKodeInstruksi() {
            return kodeInstruksi;
        }

        public void setKodeInstruksi(String kodeInstruksi) {
            this.kodeInstruksi = kodeInstruksi;
        }
    }
}