package com.belajar.springboot.sb.controller;

import com.belajar.springboot.common.dto.WebResponse;
import com.belajar.springboot.sb.dto.BoxBayarRequest;
import com.belajar.springboot.sb.dto.BoxResponse;
import com.belajar.springboot.sb.dto.BoxSewaRequest;
import com.belajar.springboot.sb.service.SafeDepositBoxService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/box")
public class SafeDepositBoxController {

    @Autowired
    private SafeDepositBoxService boxService;

    @GetMapping
    public ResponseEntity<WebResponse<List<BoxResponse>>> inquiry(
            @RequestParam(value = "status", required = false) String status) {

        List<BoxResponse> data = boxService.inquiry(status);
        WebResponse<List<BoxResponse>> response = WebResponse.<List<BoxResponse>>builder()
                .status("SUCCESS")
                .message("Inquiry box berhasil")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sewa")
    public ResponseEntity<WebResponse<BoxResponse>> sewa(
            @Valid @RequestBody BoxSewaRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {

        BoxResponse data = boxService.sewa(request, operatorId);
        WebResponse<BoxResponse> response = WebResponse.<BoxResponse>builder()
                .status("SUCCESS")
                .message("Box " + data.getNomorBox() + " berhasil disewa hingga " + data.getTanggalJatuhTempo())
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{nomorBox}/bayar")
    public ResponseEntity<WebResponse<BoxResponse>> bayarPerpanjang(
            @PathVariable("nomorBox") String nomorBox,
            @RequestBody @Valid BoxBayarRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {

        request.setNomorBox(nomorBox);
        BoxResponse data = boxService.bayarPerpanjang(request, operatorId);
        WebResponse<BoxResponse> response = WebResponse.<BoxResponse>builder()
                .status("SUCCESS")
                .message("Pembayaran sewa box " + nomorBox + " berhasil, tempo menjadi " + data.getTanggalJatuhTempo())
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{nomorBox}/kembali")
    public ResponseEntity<WebResponse<BoxResponse>> kembalikan(
            @PathVariable("nomorBox") String nomorBox,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {

        BoxResponse data = boxService.kembalikan(nomorBox, operatorId);
        WebResponse<BoxResponse> response = WebResponse.<BoxResponse>builder()
                .status("SUCCESS")
                .message("Box " + nomorBox + " kembali TERSEDIA")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}