package com.belajar.springboot.dp.service;

import com.belajar.springboot.dp.dto.CreateTransaksiRequest;
import com.belajar.springboot.dp.dto.TransaksiSummaryProjection;
import com.belajar.springboot.dp.dto.TransaksiSummaryResponse;
import com.belajar.springboot.dp.entity.Transaksi;
import com.belajar.springboot.dp.repository.TransaksiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;

    @Transactional(readOnly = true)
    public List<TransaksiSummaryResponse> getMonthlySummary(Integer year, String nomorRekening) {
        List<TransaksiSummaryProjection> projections = transaksiRepository.getMonthlySummary(year, nomorRekening);

        return projections.stream().map(p -> {
            String namaBulan = Month.of(p.getBulan())
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // e.g. "Feb", "Mar"
            String periode = namaBulan + " " + p.getTahun(); // e.g. "Feb 2025"

            return TransaksiSummaryResponse.builder()
                    .tahun(p.getTahun())
                    .bulan(p.getBulan())
                    .periode(periode)
                    .totalTransaksi(p.getTotalCount())
                    .totalNominal(p.getTotalAmount())
                    .totalDebit(p.getTotalDebit())
                    .totalKredit(p.getTotalKredit())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public Transaksi createTransaksi(CreateTransaksiRequest request) {
        Transaksi transaksi = Transaksi.builder()
                .nomorRekening(request.getNomorRekening())
                .tipeTransaksi(request.getTipeTransaksi())
                .amount(request.getAmount())
                .deskripsi(request.getDeskripsi())
                .createdAt(request.getCreatedAt())
                .build();

        return transaksiRepository.save(transaksi);
    }

    @Transactional(readOnly = true)
    public List<Transaksi> getHistory(String nomorRekening) {
        if (nomorRekening != null && !nomorRekening.isBlank()) {
            return transaksiRepository.findByNomorRekeningOrderByCreatedAtDesc(nomorRekening);
        }
        return transaksiRepository.findAll();
    }
}
