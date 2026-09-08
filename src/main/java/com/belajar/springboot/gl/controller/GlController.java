package com.belajar.springboot.gl.controller;

import com.belajar.springboot.common.dto.WebResponse;
import com.belajar.springboot.gl.dto.BukuBesarResponse;
import com.belajar.springboot.gl.dto.CoaResponse;
import com.belajar.springboot.gl.dto.CreateCoaRequest;
import com.belajar.springboot.gl.dto.JurnalRequest;
import com.belajar.springboot.gl.dto.LabaRugiResponse;
import com.belajar.springboot.gl.dto.NeracaResponse;
import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.service.BukuBesarService;
import com.belajar.springboot.gl.service.CoaService;
import com.belajar.springboot.gl.service.JurnalService;
import com.belajar.springboot.gl.service.LabaRugiService;
import com.belajar.springboot.gl.service.NeracaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GlController {

    @Autowired
    private NeracaService neracaService;

    @Autowired
    private CoaService coaService;

    @Autowired
    private JurnalService jurnalService;

    @Autowired
    private LabaRugiService labaRugiService;

    @Autowired
    private BukuBesarService bukuBesarService;

    @GetMapping("/neraca")
    public ResponseEntity<WebResponse<NeracaResponse>> getNeraca(
            @RequestParam(value = "tanggal", required = false) String tanggal) {

        LocalDate tgl = parseTanggal(tanggal);
        NeracaResponse data = neracaService.getNeraca(tgl);

        WebResponse<NeracaResponse> response = WebResponse.<NeracaResponse>builder()
                .status("SUCCESS")
                .message("Neraca per " + data.getTanggal() + " berhasil dibuat")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/coa")
    public ResponseEntity<WebResponse<List<CoaResponse>>> daftarAkun(
            @RequestParam(value = "tanggal", required = false) String tanggal) {

        LocalDate tgl = parseTanggal(tanggal);
        List<CoaResponse> data = neracaService.daftarAkun(tgl);

        WebResponse<List<CoaResponse>> response = WebResponse.<List<CoaResponse>>builder()
                .status("SUCCESS")
                .message("Berhasil mengambil daftar akun")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/coa")
    public ResponseEntity<WebResponse<CoaResponse>> createCoa(
            @Valid @RequestBody CreateCoaRequest request) {

        Coa coa = coaService.create(request);

        CoaResponse data = CoaResponse.builder()
                .id(coa.getId())
                .kodeAkun(coa.getKodeAkun())
                .namaAkun(coa.getNamaAkun())
                .jenisAkun(coa.getJenisAkun())
                .posisiNormal(coa.getPosisiNormal())
                .aktif(coa.getAktif())
                .build();

        WebResponse<CoaResponse> response = WebResponse.<CoaResponse>builder()
                .status("SUCCESS")
                .message("Akun GL " + coa.getKodeAkun() + " berhasil dibuat!")
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/jurnal")
    public ResponseEntity<WebResponse<String>> postJurnal(
            @Valid @RequestBody JurnalRequest request,
            @RequestHeader(value = "X-Operator-Id", required = false, defaultValue = "SYSTEM") String operatorId) {

        String ref = jurnalService.postJurnal(request, operatorId);
        WebResponse<String> response = WebResponse.<String>builder()
                .status("SUCCESS")
                .message("Jurnal " + ref + " berhasil diposting!")
                .data(ref)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/laba-rugi")
    public ResponseEntity<WebResponse<LabaRugiResponse>> getLabaRugi(
            @RequestParam(value = "tanggal", required = false) String tanggal) {

        LocalDate tgl = parseTanggal(tanggal);
        LabaRugiResponse data = labaRugiService.getLabaRugi(tgl);

        WebResponse<LabaRugiResponse> response = WebResponse.<LabaRugiResponse>builder()
                .status("SUCCESS")
                .message("Laba rugi per " + data.getTanggal() + " berhasil dibuat")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/buku-besar")
    public ResponseEntity<WebResponse<BukuBesarResponse>> getBukuBesar(
            @RequestParam(value = "kodeAkun", required = false) String kodeAkun,
            @RequestParam(value = "dari", required = false) String dari,
            @RequestParam(value = "sampai", required = false) String sampai) {

        LocalDate d = parseTanggal(dari);
        LocalDate s = parseTanggal(sampai);
        BukuBesarResponse data = bukuBesarService.getBukuBesar(kodeAkun, d, s);

        WebResponse<BukuBesarResponse> response = WebResponse.<BukuBesarResponse>builder()
                .status("SUCCESS")
                .message("Buku besar berhasil dibuat")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    private LocalDate parseTanggal(String tanggal) {
        if (tanggal == null || tanggal.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(tanggal);
    }
}