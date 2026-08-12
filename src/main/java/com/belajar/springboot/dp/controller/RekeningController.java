package com.belajar.springboot.dp.controller;

import com.belajar.springboot.common.dto.WebResponse;
import com.belajar.springboot.dp.dto.CreateRekeningRequest;
import com.belajar.springboot.dp.dto.RekeningResponse;
import com.belajar.springboot.dp.dto.TransferRequest;
import com.belajar.springboot.dp.service.RekeningService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rekening")
public class RekeningController {

    @Autowired
    private RekeningService rekeningService;

    @PostMapping("/create")
    public ResponseEntity<WebResponse<RekeningResponse>> createRekening(
            @Valid @RequestBody CreateRekeningRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {
        
        RekeningResponse data = rekeningService.createRekening(request, operatorId);
        WebResponse<RekeningResponse> response = WebResponse.<RekeningResponse>builder()
                .status("SUCCESS")
                .message("Rekening baru berhasil dibuka!")
                .data(data)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<WebResponse<String>> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "TELLER1") String operatorId) {
        
        rekeningService.transfer(request, operatorId);
        WebResponse<String> response = WebResponse.<String>builder()
                .status("SUCCESS")
                .message(String.format("Transfer sebesar Rp %s dari Rekening %s ke %s BERHASIL!", 
                        request.getNominal(), request.getNomorRekeningAsal(), request.getNomorRekeningTujuan()))
                .data("TRANSFER_SUCCESS")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cif/{cif}")
    public ResponseEntity<WebResponse<List<RekeningResponse>>> getRekeningByCif(@PathVariable("cif") String cif) {
        List<RekeningResponse> list = rekeningService.getRekeningByCif(cif);
        WebResponse<List<RekeningResponse>> response = WebResponse.<List<RekeningResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil daftar rekening CIF " + cif)
                .data(list)
                .build();
        
        return ResponseEntity.ok(response);
    }
}
